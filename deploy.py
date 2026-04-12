# /// script
# dependencies = ["click"]
# requires-python = ">=3.10"
# ///
"""Braveheart Timer — build & deploy CLI."""

import subprocess
import sys

import click

JAVA_HOME = "/usr/local/opt/openjdk@17"


def run(cmd: str, **kwargs) -> subprocess.CompletedProcess:
    """Run a shell command with JAVA_HOME set."""
    env_prefix = f"JAVA_HOME={JAVA_HOME} "
    result = subprocess.run(
        env_prefix + cmd if "gradlew" in cmd else cmd,
        shell=True,
        text=True,
        capture_output=kwargs.pop("capture", False),
        **kwargs,
    )
    return result


@click.group()
def cli():
    """Braveheart Timer build & deploy tool."""


@cli.command()
@click.argument("addr", metavar="IP:PORT")
def pair(addr: str):
    """Pair with a device via wireless debugging.

    Get the IP:PORT and pairing code from:
    Settings > Developer Options > Wireless Debugging > Pair device with pairing code
    """
    click.echo(f"Pairing with {addr}...")
    run(f"adb pair {addr}")


@cli.command()
@click.argument("addr", metavar="IP:PORT")
def connect(addr: str):
    """Connect to a paired device.

    Use the IP:PORT shown on the main Wireless Debugging screen
    (different from the pairing port).
    """
    click.echo(f"Connecting to {addr}...")
    run(f"adb connect {addr}")


@cli.command()
def devices():
    """List connected devices."""
    run("adb devices -l")


@cli.command()
@click.option("--clean", is_flag=True, help="Clean build before deploying.")
def push(clean: bool):
    """Build and install the app on a connected device."""
    if clean:
        click.echo("Cleaning...")
        result = run("./gradlew clean")
        if result.returncode != 0:
            sys.exit(1)

    click.echo("Building and installing...")
    result = run("./gradlew installDebug")
    if result.returncode != 0:
        click.secho("Build failed.", fg="red")
        sys.exit(1)

    click.secho("Installed successfully!", fg="green")


@cli.command()
def build():
    """Build the debug APK without installing."""
    click.echo("Building...")
    result = run("./gradlew assembleDebug")
    if result.returncode != 0:
        click.secho("Build failed.", fg="red")
        sys.exit(1)
    click.secho("APK: app/build/outputs/apk/debug/app-debug.apk", fg="green")


@cli.command()
def test():
    """Run unit tests and Paparazzi screenshot tests."""
    click.echo("Running tests...")
    result = run("./gradlew :app:testDebugUnitTest")
    if result.returncode != 0:
        click.secho("Tests failed.", fg="red")
        sys.exit(1)
    click.secho("All tests passed!", fg="green")


if __name__ == "__main__":
    cli()
