# -*- coding: utf-8 -*-
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = ROOT / "ruoyi-ui" / "src" / "views" / "medical" / "viewer" / "index.vue"
raw = p.read_bytes()
s = None
used = None
for enc in ("utf-8", "gbk", "utf-8-sig"):
    try:
        s = raw.decode(enc)
        used = enc
        break
    except UnicodeDecodeError:
        continue
if s is None:
    s = raw.decode("utf-8", "replace")
    used = "utf-8-replace"
# Heuristic: UTF-8 decode succeeded but body is actually GBK bytes misread
if used == "utf-8" and "goPatientList" in s:
    idx = raw.find(b"goPatientList\">")
    if idx >= 0:
        chunk = raw[idx + len(b"goPatientList\">") : idx + len(b"goPatientList\">") + 24]
        if chunk and chunk[0] >= 0x80:
            try:
                s2 = raw.decode("gbk")
            except UnicodeDecodeError:
                s2 = None
            if s2 and ("\u8fd4\u56de" in s2 or "\u60a3\u8005" in s2):
                s, used = s2, "gbk"

s = s.replace("(px?)", "(px\u00b2)")
s = s.replace("\u300cAI\u3000\u75c5", "\u300cAI \u75c5")
p.write_text(s, encoding="utf-8", newline="\n")
print("wrote utf-8, source_decode=", used)
