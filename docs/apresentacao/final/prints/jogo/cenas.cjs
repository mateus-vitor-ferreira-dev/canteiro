// Os cenários dos prints "No jogo". Uso: OUT=<pasta> URL=<endereço do jogo> node cenas.cjs <normal|alerta|pesado|rotacao|fim|telas> [nomes...]
// Precisa do puppeteer-core (npm i puppeteer-core) e de um Chrome. Os recortes de cada print estão no README da apresentação.
const L=require("./robo.cjs");const w=L.w;
const cena=process.argv[2];
const vivo=g=>g.estado&&g.estado.estado==="PECA_CAINDO";
async function esperaVivo(g){for(let i=0;i<40&&!vivo(g);i++)await w(50)}
(async()=>{const g=await L.abrir();
try{
if(cena==="normal"){ // pontuação, reserva, fila, estabilidade, centro
  await g.partida("Normal");let fotosLinha=0,reservou=false;
  for(let i=0;i<40&&fotosLinha<4;i++){await esperaVivo(g);if(!vivo(g))break;
    if(i===14&&!reservou){await g.tecla("c",250);reservou=true;await g.foto("reserva");}
    if(i===10)await g.foto("meio");
    const lin=g.estado.placar.linhas;await L.jogarUma(g);
    if(i===9){await w(100);await g.foto("previa")}
    await g.tecla("Space",20);
    for(let k=0;k<8;k++){await w(25);if(g.estado.placar.linhas>lin){await w(260);await g.foto("linha-"+(fotosLinha++));break}} await w(120);}
}
if(cena==="alerta"){
  await g.partida("Normal");let i=0;
  for(;i<6;i++){await esperaVivo(g);await L.jogarUma(g,{l:-5,b:9,h:0.3,u:0.6,d:6,m:1});await g.tecla("Space",150);}
  let fase=0;
  for(;i<40;i++){await esperaVivo(g);if(!vivo(g))break;
    await L.jogarUma(g,{l:-5,b:2,h:0,u:0,d:-8,m:0.2},(pos)=>pos.every(([r,c])=>c>=6));
    await w(130);const e=g.estado.estabilidade;
    if(e.alerta&&fase===0){await g.foto("alerta-"+i);fase=1}
    if(e.previa&&e.previa.derruba){await g.foto("derruba-"+i);const n0=g.eventos.length;await g.tecla("Space",5);
      let col=false;for(let k=0;k<40;k++){await w(10);if(g.eventos.slice(n0).some(x=>x.evento==="COLAPSO")){col=true;break}}
      if(col){let t0=0;for(const t of [40,150,300,500,750,1100]){await w(t-t0);t0=t;await g.foto("colapso-"+t)}console.log("colapso",JSON.stringify(g.eventos.find(x=>x.evento==="COLAPSO")).slice(0,300));break}
      await w(150);continue}
    await g.tecla("Space",150);}
}
if(cena==="pesado"){ // procura jogada que quebra ou esmaga
  await g.partida("Difícil");let achouQ=0,achouE=0;
  for(let i=0;i<60&&(achouQ<2||achouE<2);i++){await esperaVivo(g);if(!vivo(g))break;
    const mat=g.estado.pecaAtual.material;let feito=false;
    if(mat==="CONCRETO"||mat==="ACO"){
      // varre colunas da esquerda à direita
      for(let k=0;k<6;k++)await g.tecla("ArrowLeft",35);
      for(let c=0;c<10&&!feito;c++){await w(70);const pv=g.estado.estabilidade.previa;if(!pv)continue;
        if(pv.esmagados.length&&achouE<2){await g.foto(`esmaga-${i}-antes`);await g.tecla("Space",10);for(const t of [120,400,900]){await w(t===120?120:t===400?280:500);await g.foto(`esmaga-${i}-${t}`)}achouE++;feito=true;break}
        if(pv.quebras.length&&achouQ<2&&!pv.derruba){await g.foto(`quebra-${i}-antes`);await g.tecla("Space",10);for(const t of [120,400,900]){await w(t===120?120:t===400?280:500);await g.foto(`quebra-${i}-${t}`)}achouQ++;feito=true;break}
        await g.tecla("ArrowRight",35);}
    }
    if(!feito){ // madeira: torre no meio; resto: bot
      if(mat==="MADEIRA"){await L.jogarUma(g,{l:2,b:2,h:0.1,u:0.1,d:4,m:0})}else await L.jogarUma(g);
      await g.tecla("Space",150);}
  }
  console.log("quebras",achouQ,"esmagos",achouE);
}
if(cena==="rotacao"){
  await g.partida("Fácil");
  for(let i=0;i<14;i++){await esperaVivo(g);const f=g.estado.pecaAtual.forma;
    if(f==="I"){await g.tecla("ArrowUp",80);for(let k=0;k<6;k++)await g.tecla("ArrowLeft",45);for(let k=0;k<5;k++)await g.tecla("ArrowDown",40);await w(100);console.log(JSON.stringify(g.estado.pecaAtual.blocos));await g.foto("rot-antes");await g.tecla("ArrowUp",120);console.log(JSON.stringify(g.estado.pecaAtual.blocos));await g.foto("rot-depois");break}
    await L.jogarUma(g);await g.tecla("Space",150);}
}
if(cena==="fim"){
  const nomes=process.argv.slice(3);
  for(const nome of nomes){
    await g.partida(nome==="ultimo"?"Normal":"Fácil");
    for(let i=0;i<200;i++){if(g.estado.estado!=="PECA_CAINDO"){await w(100);if(g.estado.estado!=="PECA_CAINDO"&&g.estado.estado!=="LINHAS_ELIMINANDO"&&i>3)break;continue}
      // joga bem por um tempo e depois só no meio
      if(i<(nome==="ultimo"?18:8+nomes.indexOf(nome)*4)){await L.jogarUma(g);}await g.tecla("Space",60);}
    await w(600);await g.foto("fim-camada-"+nome);
    await g.clicar("Ver resultado");await w(900);await g.foto("fim-resultado-"+nome);
    if(nome!=="ultimo"){await g.pg.type("input[type=text], input:not([type])",nome);await g.clicar("Salvar no ranking");await w(800);await g.foto("fim-salvo-"+nome);await g.clicar("Voltar ao menu");await w(600)}
  }
}
if(cena==="telas"){
  await g.clicar("Ranking");await w(900);await g.foto("ranking");await g.clicar("Voltar");await w(500);
  await g.clicar("Repetições");await w(900);await g.foto("repeticoes");
  const ok=await g.pg.evaluate(()=>{const b=[...document.querySelectorAll("button")].find(b=>/Assistir|Ver/.test(b.innerText));b&&b.click();return b&&b.innerText});console.log("abriu",ok);
  await w(4000);await g.foto("repeticao-tocando");
}
}catch(e){console.log("ERRO",e.message)}
console.log(cena,JSON.stringify(g.estado&&g.estado.placar),g.estado&&g.estado.estado);await g.b.close();})();
