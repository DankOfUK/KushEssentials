# KushEssentials

A from-scratch Essentials-style core for **Paper 1.21.4** (Java 21), with staff/regular
feature separation, a kit manager, a ranks + permissions system, a custom tab list,
a custom server-list MOTD, first-join welcome titles, and optional PlaceholderAPI support.

All original code (not EssentialsX source).

## Building

Requires Maven and JDK 21.

```bash
mvn clean package
```

Output: `target/KushEssentials-0.0.1-ALFA.jar` → drop into your server's `plugins/`.
On first run it writes `config.yml`, `kits.yml`, `messages.yml`, and `ranks.yml`
into `plugins/KushEssentials/`, plus data files (`homes.yml`, `warps.yml`,
`spawn.yml`, `userdata.yml`).

PlaceholderAPI is an optional `provided` dependency — the plugin loads and runs fine
without it; placeholder text is simply left unresolved when it's absent.

## Feature map

| Area | Config | Notes |
|---|---|---|
| Homes / warps / spawn / tpa / back | `config.yml` → `regular.*` | per-rank home limits via `essentials.homes.<n>` |
| Heal/fly/gamemode/repair/vanish/god/freeze/moderation | `config.yml` → `staff.*` | each subsystem has its own `enabled` flag |
| Kits | `kits.yml` | per-item meta, armor, command hooks, cooldowns, first-join kit |
| **Ranks & permissions** | `ranks.yml` | prefix/suffix/color/weight, inheritance, live permission attach |
| **Tab list** | `config.yml` → `tab` | header/footer, rank-colored names, rank-weight sorting |
| **MOTD** | `config.yml` → `motd` | two-line server-list MOTD with `{online}`/`{max}` |
| **Welcome title** | `config.yml` → `welcome` | join title + separate first-join variant + join messages |
| **PlaceholderAPI** | — | resolves `%papi%` in tab/title; exposes `%kush_rank*%` |
| **Economy** | `config.yml` → `economy` | `/balance`, `/pay`, `/baltop`, admin `/eco`; registers as a **Vault** provider |

## Ranks

`ranks.yml` defines each rank's `display`, `prefix`, `suffix`, `color`, `weight`
(lower = higher in tab), optional `inherits`, and `permissions`. A player's rank is
stored in `userdata.yml`; on join (and on `/rank set`) the resolved permission set is
attached live, so changes apply with no relog. New players get `default-rank`.

Commands: `/rank list`, `/rank info [player]`, `/rank set <player> <rank>`,
`/rank reload`. Set/reload need `essentials.rank.admin`; list/info need
`essentials.rank` (default true).

## Tab, MOTD & welcome text tokens

In `tab`, `motd`, and `welcome` text you can use:
- `{player}`, `{online}`, `{max}` — always available, no dependency
- any `%placeholder%` from PlaceholderAPI when it's installed (e.g. `%player_name%`,
  `%vault_rank%`, `%server_tps%`, plus this plugin's own `%kush_rank%`,
  `%kush_rank_prefix%`, `%kush_rank_suffix%`, `%kush_rank_color%`, `%kush_rank_weight%`)

PlaceholderAPI placeholders need their expansion installed server-side
(e.g. `/papi ecloud download Player`); the built-in `{player}` token avoids that.

## Permission bundles

- `essentials.player` (default true) — all regular features
- `essentials.staff` (op) — all staff features
- `essentials.admin` (op) — warp/spawn/reload/rank admin
- `essentials.*` (op) — everything

Hand these out per-rank in `ranks.yml`.

## Reloading

`/ess reload` reloads config & messages. `/rank reload` reloads `ranks.yml` and
re-applies permissions to everyone online.

## Economy & Vault

Balances are stored in `userdata.yml` (under `economy.<uuid>`), rounded to 2 decimals.
Commands: `/balance [player]` (aliases `/bal`, `/money`), `/pay <player> <amount>`,
`/baltop`, and admin `/eco give|take|set|reset <player> [amount]` (alias `/economy`).

If **Vault** is installed, KushEssentials registers itself as the economy provider on
startup, so Vault-aware plugins (shops, jobs, etc.) read and write the same balances.
Vault is optional — without it, the economy commands still work; only the cross-plugin
bridge is skipped. Banks are not supported (Vault bank calls return `NOT_IMPLEMENTED`).

Both Vault and PlaceholderAPI are `provided` dependencies pulled at build time
(Vault via JitPack); neither needs to be present at runtime for the plugin to load.
