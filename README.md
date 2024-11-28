# Mineconomy

[English](./README.md) | [한국어](./README.ko.md)

Mineconomy is an open-source plugin project designed to build an economic system for Minecraft servers. It offers various features including a currency system, stock trading, banking, shops, and more.

<details>
<summary>

## Features List
</summary>

### &#9745; **Currency System (Core)**: 
- Manages the virtual currency **Mark** for players and companies.
- Players can earn currency by mining or selling resources.
  
### &#9744; **Stock Trading System**:
- Allows players to invest and trade stocks.
- Enables prediction and trading of resource values through a futures options trading system.

### &#9744; **Banking System**:
- Provides features for loans, investments, and credit ratings.
- Manages the flow of the server’s economy and the players' financial activities.

### &#9744; **Shop System**:
- Provides a shop system where resources can be bought and sold on the server.
- Players can trade and purchase items through the shop.

### &#9744; **Admin Tools**:
- Provides administrative tools for managing the server economy.
    - [x] **Mark Management**: Allows admins to set and track player and company currency, as well as transactions and balances.
    - [ ] **Economic Statistics**: View real-time statistics of the server's overall economy.
    - [ ] **Policy Settings**: Adjust policies based on economic changes.

### &#9744; **GUI Support**:
- Provides GUI interfaces for users to easily interact with economic features.
    - [ ] **Stock GUI**: A GUI for stock trading.
    - [ ] **Banking GUI**: A GUI for viewing loan and investment details.
    - [ ] **Shop GUI**: A GUI for easily buying or selling shop items.

### &#9744; **RPG Features**:
- Adds features for economic activities through dungeons and additional monsters. (Under development)

### &#9744; **Server Statistics Visualization**:
- Provides a dashboard to visualize the server’s economic state and trends. (Under development)

</details>

## Installation Instructions
1. Download the [plugin](https://github.com/Nekonic/Mineconomy/releases/) and add it to the `plugins` folder.
2. Copy the resource pack URL and paste it into the `resource-pack=` field in the `server.properties` file.
3. Set `require-resource-pack=` to `true`.
4. Use the following command to encrypt the resource pack file to SHA-1, and paste the code in the `resource-pack-sha1=` field:
```shell
certutil -hashfile .\Mineconomy-Resource-Pack.zip sha1
```
5. Complete the initial setup through the config.yml file.
6. Restart the server or use the /reload confirm command to enable the plugin.

## Contributing
Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file to learn how you can contribute.
