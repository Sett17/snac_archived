if(!self.define){let e,s={};const i=(i,r)=>(i=new URL(i+".js",r).href,s[i]||new Promise((s=>{if("document"in self){const e=document.createElement("script");e.src=i,e.onload=s,document.head.appendChild(e)}else e=i,importScripts(i),s()})).then((()=>{let e=s[i];if(!e)throw new Error(`Module ${i} didn’t register its module`);return e})));self.define=(r,o)=>{const c=e||("document"in self?document.currentScript.src:"")||location.href;if(s[c])return;let t={};const n=e=>i(e,c),d={module:{uri:c},exports:t,require:n};s[c]=Promise.all(r.map((e=>d[e]||n(e)))).then((e=>(o(...e),t)))}}define(["./workbox-d172407d"],(function(e){"use strict";self.addEventListener("message",(e=>{e.data&&"SKIP_WAITING"===e.data.type&&self.skipWaiting()})),e.precacheAndRoute([{url:"copilot.css",revision:"462fbbfd1817d9508b193f09cc763e96"},{url:"copilot.sass",revision:"aca9213c33cdb182d403c2e680080847"},{url:"FiraCode.ttf",revision:"f87da900a65298f75ace319acf96abfd"},{url:"login.js",revision:"2cbadf22c1d77c836d5d2904b7abc786"},{url:"style.css",revision:"9dc4d8e8dbb3149eb66eb0b15e92daa5"},{url:"style.sass",revision:"572ee57485bbe5874f3cc4fae63027ff"}],{ignoreURLParametersMatching:[/^utm_/,/^fbclid$/]})}));
//# sourceMappingURL=sw.js.map