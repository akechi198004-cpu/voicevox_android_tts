# Third-party notes for this POC

This source project intentionally does **not** redistribute VOICEVOX model/runtime binaries in the ZIP. `scripts/prepare-voicevox.*` downloads the selected official artifacts directly from VOICEVOX release pages.

Prepared runtime components (verified against official 0.17.0 artifacts):

- VOICEVOX Core / Java Android package 0.17.0 — MIT License.
- VOICEVOX ONNX Runtime 1.23.2 Android arm64 CPU build — VOICEVOX ONNX Runtime terms.
- VOICEVOX VVM 0.17.0: `0.vvm`, `4.vvm`, `6.vvm`, `8.vvm` — VOICEVOX VVM terms.
- Open JTalk dictionary `open_jtalk_dic_utf_8-1.11`.
- Voice credits used by this POC: `VOICEVOX:WhiteCUL`, `VOICEVOX:四国めたん`, `VOICEVOX:玄野武宏`, `VOICEVOX:No.7`.

Official VVM 0.17.0 terms (accepted via the official downloader) require:

- Commercial and non-commercial use of the models is allowed.
- Redistribution inside an application is allowed.
- Generated audio must follow each character voice-library terms.
- A credit that makes VOICEVOX usage obvious is required.

Character terms used by this POC (from the official 0.17.0 agreement text):

- WhiteCUL: credit `VOICEVOX:WhiteCUL`. Commercial / non-commercial use allowed. Details: https://www.whitecul.com/guideline
- 四国めたん: credit `VOICEVOX:四国めたん`. Commercial / non-commercial use allowed. Details: https://zunko.jp/con_ongen_kiyaku.html
- 玄野武宏: credit `VOICEVOX:玄野武宏`. Commercial / non-commercial use allowed. Details: https://www.virvoxproject.com/voicevoxの利用規約
- No.7: credit `VOICEVOX:No.7`. Personal non-commercial use (including doujin / streaming income) is allowed. Other commercial use requires prior confirmation from  No.7 製作委員会: https://voiceseven.com/#j0200

`0.vvm` also contains other speakers (ずんだもん / 春日部つむぎ / 雨晴はう). `4.vvm` also contains 剣崎雌雄. This POC does not expose those extra styles in the UI, but the files are the official bundled VVMs.

Before commercial delivery, re-check the latest upstream license/terms and preserve all required notices/credits.
