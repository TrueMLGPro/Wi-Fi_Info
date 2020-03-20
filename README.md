# Wi-Fi Info
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![GitHub release](https://img.shields.io/github/release/TrueMLGPro/Wi-Fi_Info.svg)](https://GitHub.com/TrueMLGPro/Wi-Fi_Info/releases/)
[![Github all releases](https://img.shields.io/github/downloads/TrueMLGPro/Wi-Fi_Info/total.svg)](https://api.github.com/TrueMLGPro/Wi-Fi_Info/releases/all/)
[![GitHub contributors](https://img.shields.io/github/contributors/TrueMLGPro/Wi-Fi_Info.svg)](https://GitHub.com/TrueMLGPro/Wi-Fi_Info/graphs/contributors/)
[![GitHub issues](https://img.shields.io/github/issues/TrueMLGPro/Wi-Fi_Info.svg)](https://github.com/TrueMLGPro/Wi-Fi_Info/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/TrueMLGPro/Wi-Fi_Info.svg)](https://GitHub.com/TrueMLGPro/Wi-Fi_Info/issues?q=is%3Aissue+is%3Aclosed)
[![Website truemlgpro.github.io](https://img.shields.io/website-up-down-green-red/https/truemlgpro.github.io/Wi-Fi_Info.svg)](https://truemlgpro.github.io/Wi-Fi_Info/)
[![Discord](https://img.shields.io/discord/601107291915419658.svg)](https://discord.gg/qxE2DFr)
### Wi-Fi Info Android Project

***Shows information about network you are connected to.***

* __SSID__
* __BSSID__
* __Gateway IP__
* __IPv4__
* __IPv6__
* __DNS (1)__
* __DNS (2)__
* __Subnet Mask__
* __Lease Duration__
* __RSSI__
* __Frequency__
* __Network Speed__
* __Network ID__
* __MAC Address__
* __WPA Supplicant State__

Go to **[Releases](https://github.com/TrueMLGPro/Wi-Fi_Info/)** to get the latest app version! **(1.3_b)**

[Discord Server](https://discord.gg/qxE2DFr)

# TODO List

- [x] Remake "No Connection" detection method **(1.3)**
- [x] Revamp DiscordServersActivity **(1.3)**
- [x] Rename servers on DiscordServersActivity **(1.3)**
- [x] Add Developers/Supporters activity **(1.3)**
- [x] Add name at the bottom of SplashActivity **(1.3)**
- [x] Add names, avatars and descriptions on SupportersActivity **(1.3)**
- [x] Implement getDhcpInfo() in MainActivity.java to get DHCP info **(1.3)**
- [x] Add ConnectionStateService.java to keep BroadcastReceiver alive **(1.3)**
- [x] Add BootReceiver.java to start ConnectionStateService on device boot (1.3)
- [x] Add Notification Action Button (Android 8+) to stop services **(1.3)**
- [x] Upgrade app to API 29 **(1.3)**
- [x] Add Notification Channel for notification in NotificationService **(1.3)**
- [x] Add IPv6 and rename "IP" to "IPv4" **(1.3)**
- [x] Add Gateway IP, DNS (1), DNS (2) and Lease Duration and Subnet Mask to info string **(1.3)**
- [x] Start ConnectionStateService as Foreground Service in separate process to prevent Android from stopping it **(1.3)**
- [x] Add AppShortcuts (home screen shortcuts) for intents **(1.3)**
- [ ] Add "Start on Device Boot" setting in SettingsActivity **(1.3)**
- [ ] Add DayNight theme to change app themes from SettingsActivity **(1.3)**
- [ ] Check if Wi-Fi is enabled on MainActivity start, show dialog if disabled **(1.3)**
- [ ] Redesign the whole app, move Toolbar menu items to NavigationDrawer, change app font style **(1.3)**
- [ ] Add animations on activity start (text fade in, moving text) **(1.3)**
- [ ] Add SettingsActivity and add NotificationService stop method **(1.3)**
- [ ] Add Notification Settings on SettingsActivity to change notification priority and update frequency **(1.3)**
- [ ] Add Total TX (Sent Packets) and Total RX (Received Packets) to info string **(1.4)**
- [ ] Add download/upload monitoring **(1.4)**
- [ ] Add Notification Layout Customizer **(1.4) (maybe)**
- [ ] Add Wi-Fi password manager **(1.4) (maybe)**
- [ ] Create a home screen widget with network info **(1.4) (maybe)**
