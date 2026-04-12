# /// script
# dependencies = ["click"]
# requires-python = ">=3.10"
# ///
"""Braveheart Timer — build & deploy CLI."""

import subprocess
import sys

import click

JAVA_HOME = "/usr/local/opt/openjdk@17"


def run(cmd: str, capture: bool = False) -> subprocess.CompletedProcess:
    """Run a shell command with JAVA_HOME set."""
    env_prefix = f"JAVA_HOME={JAVA_HOME} "
    return subprocess.run(
        env_prefix + cmd if "gradlew" in cmd else cmd,
        shell=True,
        text=True,
        capture_output=capture,
    )


def get_devices() -> list[str]:
    """Return list of connected device serial numbers."""
    result = run("adb devices", capture=True)
    devices = []
    for line in result.stdout.strip().splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            devices.append(parts[0])
    return devices


def ensure_device() -> str | None:
    """Ensure a device is connected. Returns serial or None on failure."""
    devices = get_devices()

    if len(devices) == 1:
        click.echo(f"Device: {devices[0]}")
        return devices[0]

    if len(devices) > 1:
        click.echo("Multiple devices connected:")
        for i, d in enumerate(devices, 1):
            click.echo(f"  {i}. {d}")
        choice = click.prompt("Select device", type=int, default=1)
        if 1 <= choice <= len(devices):
            return devices[choice - 1]
        click.secho("Invalid selection.", fg="red")
        return None

    # No devices — walk through connection
    click.secho("No devices connected.", fg="yellow")
    if not click.confirm("Set up wireless debugging?", default=True):
        return None

    # Check if already paired
    click.echo("\nOpen: Settings > Developer Options > Wireless Debugging")
    already_paired = click.confirm("Already paired with this computer?", default=True)

    if not already_paired:
        click.echo("\nTap 'Pair device with pairing code'")
        pair_addr = click.prompt("IP:PORT from the pairing dialog")
        run(f"adb pair {pair_addr}")

    click.echo("\nNow use the IP:PORT on the main Wireless Debugging screen")
    connect_addr = click.prompt("IP:PORT to connect")
    run(f"adb connect {connect_addr}")

    devices = get_devices()
    if devices:
        click.secho(f"Connected: {devices[0]}", fg="green")
        return devices[0]

    click.secho("Connection failed. Check the IP:PORT and try again.", fg="red")
    return None


@click.group()
def cli():
    """Braveheart Timer build & deploy tool."""


@cli.command()
@click.argument("addr", metavar="IP:PORT", required=False)
def pair(addr: str | None):
    """Pair with a device via wireless debugging."""
    if not addr:
        click.echo("Open: Settings > Developer Options > Wireless Debugging > Pair device with pairing code")
        addr = click.prompt("IP:PORT shown on the pairing screen")
    click.echo(f"Pairing with {addr}...")
    run(f"adb pair {addr}")


@cli.command()
@click.argument("addr", metavar="IP:PORT", required=False)
def connect(addr: str | None):
    """Connect to a paired device."""
    if not addr:
        click.echo("Open: Settings > Developer Options > Wireless Debugging")
        addr = click.prompt("IP:PORT shown on the main wireless debugging screen")
    click.echo(f"Connecting to {addr}...")
    run(f"adb connect {addr}")


@cli.command()
def devices():
    """List connected devices."""
    run("adb devices -l")


@cli.command()
@click.option("--clean", is_flag=True, help="Clean build before deploying.")
def push(clean: bool):
    """Build and install the app on a connected device.

    If no device is connected, walks through wireless debugging setup.
    If multiple devices are connected, prompts to select one.
    """
    serial = ensure_device()
    if not serial:
        sys.exit(1)

    if clean:
        click.echo("Cleaning...")
        result = run("./gradlew clean")
        if result.returncode != 0:
            sys.exit(1)

    click.echo("Building and installing...")
    result = run(f"./gradlew installDebug -Pandroid.injected.build.api=35 -Dadb.device={serial}")
    if result.returncode != 0:
        # Fall back to plain installDebug (single device doesn't need -s)
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
