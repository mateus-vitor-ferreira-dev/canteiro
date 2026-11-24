// Funções do robô que joga o CANTEIRO pelo navegador, para os prints "No jogo" dos slides.
// Lê o estado da partida pelo WebSocket (via CDP) e escolhe cada jogada simulando a queda da peça.
const p=require("puppeteer-core");
const w=ms=>new Promise(r=>setTimeout(r,ms));
const OUT=process.env.OUT;
async function abrir(){
  const b=await p.launch({executablePath:process.env.CHROME||"/usr/bin/google-chrome",headless:true,args:["--autoplay-policy=no-user-gesture-required"]});
  const pg=await b.newPage();await pg.setViewport({width:1366,height:1000,deviceScaleFactor:2});
  const c=await pg.createCDPSession();await c.send("Network.enable");
  const g={b,pg,estado:null,eventos:[],tipos:{}};
  c.on("Network.webSocketFrameReceived",e=>{try{const m=JSON.parse(e.response.payloadData);g.tipos[m.tipo]=(g.tipos[m.tipo]||0)+1;if(m.tipo==="ESTADO")g.estado=m;else g.eventos.push(m);}catch{}});
  await pg.goto(process.env.URL||"http://127.0.0.1:7070",{waitUntil:"load"});await w(800);
  g.clicar=t=>pg.evaluate(t=>{const x=[...document.querySelectorAll("button")].find(b=>b.innerText.trim().startsWith(t));x&&x.click();return !!x},t);
  g.tecla=async(k,ms=45)=>{await pg.keyboard.press(k);await w(ms)};
  g.foto=async(nome)=>pg.screenshot({path:`${OUT}/${nome}.png`});
  g.fotoSel=async(nome,sel)=>{const e=await pg.$(sel);await e.screenshot({path:`${OUT}/${nome}.png`})};
  g.partida=async(dif,treino=false)=>{await g.clicar("Nova partida");await w(600);if(treino){await pg.evaluate(()=>{const c=[...document.querySelectorAll("input[type=checkbox]")];c.forEach(x=>{if(!x.checked)x.click()})});await w(200)}await g.clicar(dif);await w(1200)};
  return g;
}
const cols=bl=>bl.map(b=>b[1]);
// forma relativa
function norm(bl){const r0=Math.min(...bl.map(b=>b[0])),c0=Math.min(...bl.map(b=>b[1]));return bl.map(([r,c])=>[r-r0,c-c0])}
function soltar(tab,forma,c){ // retorna linhas ocupadas apos queda ou null
  const H=tab.length,W=10;const fw=Math.max(...forma.map(b=>b[1]))+1;if(c<0||c+fw>W)return null;
  let r=0;const ok=r=>forma.every(([dr,dc])=>{const R=r+dr,C=c+dc;return R<H&&!tab[R][C]});
  if(!ok(0))return null;while(ok(r+1))r++;return forma.map(([dr,dc])=>[r+dr,c+dc]);
}
function avaliar(tab,pos,peso){
  const H=tab.length;const t=tab.map(l=>l.map(x=>!!x));pos.forEach(([r,c])=>t[r][c]=true);
  let linhas=0;const t2=t.filter(l=>{const f=l.every(x=>x);if(f)linhas++;return !f});while(t2.length<H)t2.unshift(Array(10).fill(false));
  const alt=[];let buracos=0;for(let c=0;c<10;c++){let h=0,vi=false;for(let r=0;r<H;r++){if(t2[r][c]){if(!vi){h=H-r;vi=true}}else if(vi)buracos++}alt.push(h)}
  let bump=0;for(let c=0;c<9;c++)bump+=Math.abs(alt[c]-alt[c+1]);
  let n=0,s=0;for(let r=0;r<H;r++)for(let c=0;c<10;c++)if(t2[r][c]){n++;s+=c}
  const desv=n?Math.abs(s/n-4.5):0;
  return linhas*peso.l - buracos*peso.b - alt.reduce((a,b)=>a+b,0)*peso.h - bump*peso.u - desv*peso.d - Math.max(...alt)*peso.m;
}
async function formas(g){ // forma de cada rotação, girando a peça
  const out=[];for(let i=0;i<4;i++){out.push(norm(g.estado.pecaAtual.blocos));await g.tecla("ArrowUp",70)}return out;
}
async function levar(g,rot,col){
  for(let i=0;i<rot;i++)await g.tecla("ArrowUp",70);
  for(let k=0;k<12;k++){const m=Math.min(...cols(g.estado.pecaAtual.blocos));if(m===col)break;await g.tecla(m<col?"ArrowRight":"ArrowLeft",55)}
}
async function jogarUma(g,peso={l:8,b:7,h:0.5,u:0.4,d:3,m:0.5},filtro){
  const fs=await formas(g);let best=null;
  fs.forEach((f,rot)=>{for(let c=0;c<10;c++){const pos=soltar(g.estado.tabuleiro,f,c);if(!pos)continue;if(filtro&&!filtro(pos,rot,c))continue;const v=avaliar(g.estado.tabuleiro,pos,peso);if(!best||v>best.v)best={v,rot,c}}});
  if(!best)best={rot:0,c:4};
  await levar(g,best.rot,best.c);return best;
}
module.exports={abrir,w,jogarUma,levar,formas,norm,soltar,avaliar,cols};
