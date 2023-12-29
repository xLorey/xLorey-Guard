<div align="center">
    <h1>xLorey - Guard</h1>
</div>

<p align="center">
    <img alt="PZ Version" src="https://img.shields.io/badge/Project_Zomboid-v41.78.76-blue">
    <img alt="FluxLoader" src="https://img.shields.io/badge/Flux_Loader->=0.6.2-yellow">
    <img alt="Java version" src="https://img.shields.io/badge/Java-17-orange">
    <a href="https://discord.gg/BwSuTdEGJ4" style="text-decoration: none;">
         <img alt="Discord" src="https://img.shields.io/discord/1174285070761197599.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2">
    </a>
    <img alt="GitHub License" src="https://img.shields.io/github/license/xLorey/xLorey-Guard">
    <img alt="GitHub issues" src="https://img.shields.io/github/issues-raw/xlorey/xLorey-Guard">
    <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/xlorey/xLorey-Guard">
    <img alt="GitHub release (with filter)" src="https://img.shields.io/github/v/release/xlorey/xLorey-Guard">
</p>

This tool is a Flux server protection plugin. Contains a server anti-cheat that blocks basic cheats of players.

# Plugin features
> [!WARNING]
> False positives are possible, so the penalty for violations is a kick from the server

- Blocking manipulations with other people's inventory
- Blocking the use of in-game cheats (those sent via Extra packet)
- Partial blocking of BrushTool use
- Blocking cheats on cars
- Blocking cheats for skills
- Server-side player inventory synchronization (useful for future anti-cheats)

# How to use

- Download and install the latest [FluxLoader](https://github.com/xLorey/FluxLoader) version specified in the plugin requirements
- Download the latest version of the plugin from the release page
- Move the downloaded file to the `plugins` folder in the server directory
- Start the server and shut it down. A folder with the name of the project will appear in the `plugins` folder, it contains `config.yml`
- Customize the configuration file as you wish

# License

This project is licensed under `MIT license`
