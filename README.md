# Mineconomy

[English](./README.md) | [한국어](./README.ko.md)

Mineconomy is an open-source plugin project that implements a realistic economic system for Minecraft servers. Built on a single currency (원, ₩), it covers item trading via AMM, a full stock market, banking, and derivatives.

<details>
<summary>

## Features List
</summary>

### &#9744; **Currency System**:
- Single currency: **원 (₩)**, stored as `Long` with no decimal places.
- 2.5% transaction fee (truncated), collected as exchange company revenue.
- Death penalty: 3% balance loss; long offline (4+ weeks): auto-liquidation.

### &#9744; **Item Exchange (AMM · CPMM)**:
- Per-category independent liquidity pools using the **x·y=k** formula.
- Basic listing: blocks, ores, crops, mob drops.
- Admin-only listing: rare items (Elytra, Dragon's Breath).
- Weapons, armor, and enchanted items are excluded (direct trade / auction only).
- Players can deposit LP; initial pool sizes are configured in `config.yml`.

### &#9744; **Stock Market (vAMM)**:
- Virtual liquidity pools — no actual asset deposit required.
- Player-founded companies: ₩500,000 setup fee, 1,000–1,000,000 initial shares.
- IPO via public offering period (minimum 1 real-time hour).
- Common shares (voting rights) and preferred shares (dividend priority).
- Shareholder meetings: dividends, buybacks, mergers require shareholder approval.
- ±30% daily price limit per stock; ±20% circuit breaker for the whole market.
- Delisting condition: market cap falls below 5% of IPO cap for 1 week.

### &#9744; **M&A**:
- Acquire control by accumulating >50% of shares on the open market.
- Hostile takeovers allowed; poison pill defense available.
- Full merger requires simultaneous shareholder approval from both companies.

### &#9744; **Short Selling / Margin Trading**:
- Margin buy: minimum 40% collateral of held assets.
- Short sell: 105% of sell amount as collateral.
- Forced liquidation: margin ratio <20% (long) or price rise >50% above short entry.
- Short squeeze allowed; no individual short-sell cap.

### &#9744; **Banking System**:
- General loans and collateral loans (items or shares).
- Interest rate = base rate (admin-set) + credit spread.
- Repayment cycle: quarterly (every real week).
- Credit grades: A+ through D, reassessed every quarter.

### &#9744; **Derivatives** *(planned for v2)*:
- **Futures**: quarterly / semi-annual / annual settlement; cash-settled; rollover available.
- **Options**: American-style, AMM-priced, underlying: items or shares.

### &#9744; **Market Indicators**:
- Market-cap-weighted index; cap ranking; charts at 1m / 15m / 1h / 1d / 1w / all.

### &#9744; **Admin Tools**:
- Configure base interest rate, initial pool liquidity, and item tier tables.
- Real-time economic statistics dashboard.

### &#9744; **GUI Support**:
- Custom model data chest UI (no resource pack dependency for UI logic).
    - [ ] **Exchange GUI**: AMM pool trading interface.
    - [ ] **Stock GUI**: Order book and chart viewer.
    - [ ] **Banking GUI**: Loan details and credit grade overview.

### &#9745; **Vault Compatibility**:
- Thin `Economy` implementation exposing balance only.
- Full Mineconomy features available via the `mineconomy-api` module.

</details>

## Installation Instructions
1. Download the [plugin](https://github.com/Nekonic/Mineconomy/releases/) and place it in the `plugins` folder.
2. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) as a dependency.
3. Copy the resource pack URL and paste it into the `resource-pack=` field in `server.properties`.
4. Set `require-resource-pack=true`.
5. Generate the SHA-1 hash of the resource pack and paste it into `resource-pack-sha1=`:

Windows:
```shell
certutil -hashfile .\Mineconomy-Resource-Pack.zip sha1
```
Linux:
```bash
sha1sum Mineconomy-Resource-Pack.zip
```
6. Complete the initial setup in `config.yml`.
7. Restart the server or run `/reload confirm` to enable the plugin.

## Contributing
Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file to learn how you can contribute.
