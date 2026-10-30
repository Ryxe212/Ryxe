# SharkSMP CF Detector

Client-side Fabric mod project targeting Minecraft 1.21.11 and Java 21.

## Build automatically with GitHub Actions

1. Create a GitHub repository and upload the contents of this folder (the files inside `SharkSMP-CF-Detector`, not the outer folder).
2. Open the repository's **Actions** tab.
3. Select **Build SharkSMP CF Detector** and click **Run workflow** (or push to `main`/`master`).
4. When the run finishes successfully, open it and download the artifact named `sharksmp-cf-detector-1.21.11`.
5. The downloaded artifact contains the built `.jar` from `build/libs`.

## What it checks

The mod inspects the client-visible item names, lore, and item components in inventory screens and logs suspicious text that may expose a Coin Flip outcome before a click. It cannot predict a genuinely server-side hidden random result, and it cannot prove the server is secure just because no suspicious data is found.

## Local build (optional)

With Java 21 and Gradle installed, run `gradle build`. The JAR is written to `build/libs/`.

## Notes

This is a diagnostic client mod. It does not manipulate bets, alter outcomes, or bypass server protections. Lunar Client compatibility depends on whether the selected Lunar Minecraft version/profile supports loading third-party Fabric mods.
