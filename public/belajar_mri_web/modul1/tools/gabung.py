#!/usr/bin/env python3
"""Gabungkan index.html + css + js menjadi satu file HTML mandiri.
Pakai:  python tools/gabung.py   ->  dist/modul1.html"""
import re,os
root=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
r=lambda p:open(os.path.join(root,p),encoding='utf-8').read()
h=r('index.html')
h=re.sub(r"<link rel='stylesheet' href='([^']+)'>",lambda m:'<style>\n'+r(m.group(1))+'</style>',h)
h=re.sub(r"<script src='([^']+)'></script>",lambda m:'<script>\n'+r(m.group(1)).replace('</script>','<\\/script>')+'</script>',h)
os.makedirs(os.path.join(root,'dist'),exist_ok=True)
open(os.path.join(root,'dist','modul1.html'),'w',encoding='utf-8').write(h)
print('Selesai: dist/modul1.html')
