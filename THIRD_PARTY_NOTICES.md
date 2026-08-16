# Third-party notes for this POC

This source project intentionally does **not** redistribute VOICEVOX model/runtime binaries in the ZIP. `scripts/prepare-voicevox.*` downloads the selected official artifacts directly from VOICEVOX release pages.

Prepared runtime components (verified against official 0.17.0 artifacts):

- VOICEVOX Core / Java Android package 0.17.0 — MIT License.
- VOICEVOX ONNX Runtime 1.23.2 Android arm64 CPU build — VOICEVOX ONNX Runtime terms.
- VOICEVOX VVM 0.17.0: `0.vvm`, `4.vvm`, `8.vvm`, `n0.vvm` — VOICEVOX VVM terms.
- Open JTalk dictionary `open_jtalk_dic_utf_8-1.11`.
- Voice credits: `VOICEVOX:WhiteCUL`, `VOICEVOX:四国めたん`, `VOICEVOX:ずんだもん`, `VOICEVOX:春日部つむぎ`, `VOICEVOX:雨晴はう`, `VOICEVOX:玄野武宏`, `VOICEVOX:剣崎雌雄`, `VOICEVOX Nemo`.

Official VVM 0.17.0 terms (accepted via the official downloader) require:

- Commercial and non-commercial use of the models is allowed.
- Redistribution inside an application is allowed.
- Generated audio must follow each character voice-library terms.
- A credit that makes VOICEVOX usage obvious is required.

Character terms used by this product (from the official 0.17.0 agreement text):

- WhiteCUL: credit `VOICEVOX:WhiteCUL`. Commercial / non-commercial use allowed. https://www.whitecul.com/guideline
- 四国めたん: credit `VOICEVOX:四国めたん`. Commercial / non-commercial use allowed. https://zunko.jp/con_ongen_kiyaku.html
- ずんだもん: credit `VOICEVOX:ずんだもん`. Commercial / non-commercial use allowed. https://zunko.jp/con_ongen_kiyaku.html
- 春日部つむぎ: credit `VOICEVOX:春日部つむぎ`. Commercial / non-commercial use allowed. https://tsumugi-official.studio.site/rule
- 雨晴はう: credit `VOICEVOX:雨晴はう`. Follow the official 雨晴はう terms bundled with VOICEVOX 0.17.0.
- 玄野武宏: credit `VOICEVOX:玄野武宏`. Commercial / non-commercial use allowed. https://www.virvoxproject.com/voicevoxの利用規約
- 剣崎雌雄: credit `VOICEVOX:剣崎雌雄`. Commercial / non-commercial use allowed. https://frontier.creatia.cc/fanclubs/413/posts/4507
- VOICEVOX Nemo: credit `VOICEVOX Nemo`. Commercial / non-commercial use allowed. https://voicevox.hiroshiba.jp/nemo/term/

`0.vvm` also contains extra styles (あまあま / ツンツン / セクシー). `8.vvm` also contains たのしい / かなしい / びえーん. Those styles stay in the official VVM files but are not exposed by the approved-speaker whitelist.

ずんだもん remains credited because `0.vvm` contains that voice library, even if `ApprovedVoices.SHOW_ZUNDAMON` hides it from the UI.

Before commercial delivery, re-check the latest upstream license/terms and preserve all required notices/credits.
