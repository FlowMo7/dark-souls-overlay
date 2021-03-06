# (Dark Souls) Text Overlay

**Stream-overlay to include in Dark-Souls sessions for e.g. the number of deaths**

An overlay that can be managed via twitch chat or via a password-secured webpage, that e.g. moderators can access and
edit.

<p align="center">
  <img src="https://github.com/FlowMo7/dark-souls-overlay/raw/master/screenshots/DarkSoulsOverlayScreenshot.png" alt="Screenshot of the game Dark Souls with the OBS overlay of the number of deaths" />
</p>

### How to edit the displayed content

You can either define a twitch chat and a command to be used, which then moderators and the broadcaster can use to
update the shown content, e.g. using `!overlay 7 deaths`.

Alternatively, the page offers a very simplistic, password-protected page, that can be used to update the value as well
as configure the font color:

<p align="center">
  <img src="https://github.com/FlowMo7/dark-souls-overlay/raw/master/screenshots/DarkSoulsOverlaySetPageScreenshot.png" alt="Screenshot of the page to update the shown value and font color" />
</p>

## Usage

Include the overlay in OBS as `Browser Source`, and layout move / resize according to your needs. The overlay is the
index page of your hosted domain, so e.g. `https://dark-souls-overlay.example.org/`.

<p align="center">
  <img src="https://github.com/FlowMo7/dark-souls-overlay/raw/master/screenshots/DarkSoulsOverlayObsSettingsScreenshot.png" alt="Screenshot of the page to update the shown value and font color" />
</p>

To update the overlay text, navigate to `https://dark-souls-overlay.example.org/set` on your hosted service, and enter
the credentials you declared as `ADMIN_USER` and `ADMIN_PASSWORD`.

Alternatively, if you have configured a twitch chat, you can type e.g. `!overlay 8 deaths` into the configured chat (as
a broadcaster or moderator) to update the overlay to e.g. `8 deaths`.

Please note, that, when updating this via chat command, there will not be a feedback in the chat whether the action has
been performed (not like Nightbot), but the overlay will just update itself.

## Setup

The docker image can be found
here: [hub.docker.com/r/flowmo7/dark-souls-overlay](https://hub.docker.com/r/flowmo7/dark-souls-overlay).

Possible environment variables:

* `DOMAIN`: The domain this application is available at, e.g. `dark-souls-overlay.example.org`
* `IS_SECURE`: Whether this application is available as HTTPS / behind an HTTPS reverse proxy (which it should be).
  Default to `false`.
* `ADMIN_USER`: The username to access the dashboard to change the overlay content. Defaults to `admin`.
* `ADMIN_PASSWORD`: The password to access the dashboard to change the overlay content. Defaults to `password`. **You
  definitely want to change that one!**
* `TWITCH_COMMAND_CHANNEL`: The name of the twitch channel to listen for commands. If not set, will not listen on any
  chats for commands.
* `TWITCH_COMMAND_PREFIX`: The command prefix for updating the content via twitch chat. Defaults to `!overlay`.

The data is being persisted in `/var/dark-souls-backend/data`. It is advised to map this path as a volume, otherwise
your overlay data may not be persisted between application restarts.

### Example docker-compose.yml

```yaml
services:
  buzzer:
    image: "flowmo7/dark-souls-overlay:1.0.3"
    restart: unless-stopped
    ports:
      - 8080:8080 #Should be behind an SSL reverse proxy
    environment:
      - DOMAIN=dark-souls-overlay.example.org
      - IS_SECURE=true
      - ADMIN_USER=admin
      - ADMIN_PASSWORD=S0m3S3cur3P4assw0rd
    volumes:
      - /opt/docker/dark-soul-persistence:/var/dark-souls-backend/data:rw #Change host location to your persistence path
```

# LICENSE

```
Copyright 2021-2022 Florian M??tz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
