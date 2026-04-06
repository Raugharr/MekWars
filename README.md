# MekWars
MekWars is a chat and campaign engine for MegaMek. Players join MekWars servers to find opponents for MegaMek games. MekWars servers can be configured to run campaigns and scenarios - tracking player's units, experience and funds.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Guidelines](#guidelines)
- [Reporting Issues](#reporting-issues)

## Project Structure
| Name                   | Description  
| ---------------------- | -------------------------------------------------------------------------------------------------- |
| MekWarsClient          | The client application.                                                                            |
| MekWarsServer          | The server application.                                                                            |
| MekWarsCommon          | Library for common code for the client, server, HPGNet, etc.                                       |
| MekWarsDedicatedHost   | The MekWars MegaMek server, responsible for communicating game results to the server.              |
| MekWarsHPGNet Server   | that keeps track of connected MekWarsServer server's basic information such as name, player count. |
| MekWarsOperationEditor | Tool for creating and editing MekWars operations.                                                  |
| MekWarsUpdater Utility | for updating < 9.0.0 MekWars server configs to 9.0.0 standards.                                    |

## Prerequisites
Before building MekWars, ensure you have the following installed:
- **Java 11**
- **MegaMek** MekWars requires the [MegaMek](https://github.com/MegaMek/megamek/) source code to share the same parent directory as MekWars. MegaMek should be checked out on the v0.49.19.1 tag.

## Contributing

### Guidelines
 - **Code style** We use the [Google Style Guide](https://google.github.io/styleguide/javaguide.html) with a few exceptions: 4 spaces instead of 2 and [AOSP import order](https://source.android.com/docs/setup/contribute/code-style#order-import-statements).
 - **Issues first** For large features, please first open an issue to discuss the problem and your approach before spending time on an implementation.

### Reporting Issues
Found a bug? Please open an issue on the [Issues tab](https://github.com/Raugharr/MekWars/issues) and include:
 
 - A clear description of the problem.
 - Steps to reproduce.
 - Your Java version and operating system.
 - Any relevant log output or screenshots.
