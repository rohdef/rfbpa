
/* ===================== Server-del (mithf-api.php i roden) ===================== */
const API_URL = "mithf-api.php";

function apiSend(obj) {
    try {
        fetch(API_URL,{method:"POST",body:JSON.stringify(obj),keepalive:true})
            .catch(()=>{});
    } catch(e) {}
}

async function apiKald(obj){
    /* SIKKERHED: send automatisk borgerens btok med, så serveren kan udlede identiteten sikkert
     (ikke via rå kundenr). Admin-kald (med token) og pre-login-kald røres ikke. */
    try{
        if (
            obj &&
            typeof obj==="object" &&
            !obj.token &&
            !obj.btok
        ) {
            var _bt = localStorage.getItem("bpa_btok");
            if (_bt) obj.btok=_bt;
        }
    } catch (e) {}
    const r = await fetch(API_URL,{method:"POST",body:JSON.stringify(obj)});return await r.json();
}

/* 📈 Dashboard-puls: fortæller serveren at man er online — admin via token, borger via kundenr.
   Helt isoleret, fejl ignoreres, rører intet andet flow. */
(function(){
    function hfPuls(){
        try{
            var sess=null;try{sess=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
            if(sess&&sess.token){apiSend({a:"puls",token:sess.token});return;}
            var u=(window.BPA_USER||"").trim();
            if(u&&u!=="faelles")apiSend({a:"puls",u:u,n:window.BPA_NAME||""});
        }catch(e){}
    }
    setTimeout(hfPuls,2500);
    setInterval(hfPuls,60000);
    document.addEventListener("visibilitychange",function(){if(!document.hidden)hfPuls();});
})();

/* ===================== ✏️ Redigerbare borger-tekster (override fra data/tekster.json) ===================== */
const FAQ_STD=[
    {sp:"Koster det mig noget at være hos Handicapformidlingen?",svar:"Nej. Al service er omkostningsfri for dig som borger. Vores indtægt er udelukkende administrationsbeløbet fra din kommune, og hjælpernes løn følger din bevilling krone for krone."},
    {sp:"Bestemmer jeg stadig selv over min ordning?",svar:"Ja, altid. Du er arbejdsleder og bestemmer, hvilke hjælpere der ansættes, hvornår de arbejder, og hvem der eventuelt skal opsiges. Vi har det formelle arbejdsgiveransvar og hjælper med det administrative og juridiske — men styringen er din."},
    {sp:"Hvad gør jeg, hvis en vagt ikke kan dækkes — midt om natten?",svar:"Ring til vikarbooking/akuttelefonen på 71 74 70 25. Den er døgnåben, året rundt, og vi har over 2.500 aktive vikarer. Vi forsøger altid at sende en vikar, du kender i forvejen."},
    {sp:"Kan mine hjælpere beholde deres anciennitet?",svar:"Ja. Ved overgang til Handicapformidlingen tager hjælperne al anciennitet for løn og pension med sig."},
    {sp:"Hvad hvis jeg ikke har modtaget mit login til mitHF-BPA?",svar:"Tjek først din spammappe. Er det stadig væk, så kontakt din personlige konsulent eller ring på 70 777 333 — det er vigtigt, at du har login fra start."},
    {sp:"Hvornår skal vagtplanen senest være opdateret?",svar:"Lønperioden løber fra den 16. til den 15. i næste måned. Indtast vagter løbende og i god tid — senest når perioden slutter den 15. Vi sender en SMS-påmindelse hver måned omkring løn."},
    {sp:"Hvor ofte mødes jeg med min konsulent?",svar:"Så ofte du har behov — din konsulent er kun et opkald væk. Derudover tilbyder vi hvert år et fast statusmøde, hvor I sammen går småt og stort igennem: hjælperteamet, APV/MUS, vagtmøder, IT-systemet, bevillingen, forsikringer og dine ønsker til os."},
    {sp:"Hvem hjælper mig, hvis kommunen vil ændre min bevilling?",svar:"Din faste konsulent — og ved behov vores BPA Rådgivning, der har stor erfaring med ansøgninger, genvurderinger og dialog med kommunen. Kontakt os, så snart du hører fra kommunen."},
    {sp:"Kan jeg tage mine hjælpere med til udlandet?",svar:"Som udgangspunkt ja, men der gælder særlige regler. Læs <a href=\"https://handicapformidlingen.dk/regler-for-ophold-i-udlandet/\" target=\"_blank\" rel=\"noopener\">reglerne for ophold og rejser til udlandet</a>, og tal med din konsulent i god tid før afrejse."},
    {sp:"Hvordan lærer jeg systemet at kende?",svar:"Start i <a href=\"laeringshub/index.html\">læringshubben</a>, hvor du kan øve dig risikofrit med guidede demoer og øvelser. Din konsulent giver også gerne personlig sparring."}
];
var TEKSTER=null;
function faqRender(){
    const el=document.getElementById("faqListe");if(!el)return;
    const liste=(TEKSTER&&Array.isArray(TEKSTER.faq)&&TEKSTER.faq.length)?TEKSTER.faq:FAQ_STD;
    el.innerHTML=liste.map(f=>'<details><summary>'+fbaEsc(f.sp||"")+'</summary><div class="indhold">'+(f.svar||"")+'</div></details>').join("");
}

function teksterAnvend(){
    if(TEKSTER&&TEKSTER.blokke){
        Object.keys(TEKSTER.blokke).forEach(id=>{
            const el=document.querySelector('[data-tk="'+id+'"]');
            if(el&&(TEKSTER.blokke[id]||"").trim()!=="")el.innerHTML=TEKSTER.blokke[id];
        });
    }
    faqRender();
}

async function teksterHent(){
    try{const j=await apiKald({a:"teksterhent"});if(j&&j.ok){TEKSTER={blokke:j.blokke||{},faq:Array.isArray(j.faq)?j.faq:[]};}}catch(e){}
    teksterAnvend();
}

/* ===================== Adgangslås (fælles adgang.js) ===================== */
(function(){
    const gate = document.getElementById("gate");
    window.BPA_USER="faelles";
    window.BPA_NAME="";

    /* ===== POP-UD-vindue: fokuseret enkelt-område i eget vindue ===== */
    (function(){
        var pp=null;try{pp=new URLSearchParams(location.search);}catch(e){}
        var erPopout=!!(pp&&pp.get("popout")==="1");   /* fokuseret enkelt-område */
        var erDup=!!(pp&&pp.get("dup")==="1");          /* fuldt dublet-univers */
        window.HF_POPOUT=erPopout;
        if(!erPopout&&!erDup)return;
        if(erPopout){try{document.body.classList.add("popout");}catch(e){}}

        try{   /* arv admin-session: primært via localStorage-bro (robust også i installeret app), ellers fra opener */
            if(!sessionStorage.getItem("bpa_adm")){
                var s=null;
                try{s=localStorage.getItem("hf_pop_sess");}catch(e){}
                if(!s){try{if(window.opener&&window.opener.sessionStorage)s=window.opener.sessionStorage.getItem("bpa_adm");}catch(e){}}
                if(s){sessionStorage.setItem("bpa_adm",s);try{localStorage.removeItem("hf_pop_sess");}catch(e){}}
            }
            sessionStorage.setItem("adm_visning","adm");   /* pop-ud er altid et adm-område */
            var omr = pp.get("omr");if(omr)sessionStorage.setItem("omr_fokus",omr);
        }catch(e){}
    })();
    /* admin allerede logget ind i denne fane? → send videre til adminoverblikket i læringshubben */
    let admSess=null;try{admSess=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
    /* SIKKERHED (12.07): brugerlisten er IKKE længere offentlig (adgang.js spærret). Login validér
     nu 100% server-side (blogin/admlogin/admcheck), så gaten er altid låst — serveren afgør adgang.
     «Husket login» bygges nu af localStorage (bpa_user + bpa_navn), ikke af en offentlig liste. */
    const norm=s=>String(s||"").trim().toLowerCase();
    let saved="";try{saved=localStorage.getItem("bpa_user")||"";}catch(e){}
    let savedNavn="";try{savedNavn=localStorage.getItem("bpa_navn")||"";}catch(e){}
    const savedUser=saved?{k:norm(saved),n:savedNavn||saved}:null;
    /* ADMIN-SESSION HAR FORRANG (dobbelt-brugere): er man logget ind som admin og IKKE bevidst
     i 👁 borger-visning, skal man lande i adm-universet — ellers vinder det huskede borger-login
     og borger-toplinjen bliver hængende. Rene borgere (ingen bpa_adm) er 100% uændret. */
    let admVisn0="adm";try{admVisn0=sessionStorage.getItem("adm_visning")||"adm";}catch(e){}
    const admHarForrang=!!(admSess&&admSess.token&&admVisn0!=="borger");
    if(savedUser&&!admHarForrang){
        window.BPA_USER=savedUser.k;window.BPA_NAME=savedUser.n;gate.remove();
        try{apiKald({a:"ssomig",u:savedUser.k}).then(function(_s){window.BPA_SSO_AKTIV=!!(_s&&_s.aktiv);}).catch(function(){});}catch(e){}   /* må denne borger logge ind i mitHF? (styrer mitHF-knappen) */
        apiSend({a:"log",t:"login",u:savedUser.k,n:savedUser.n});
        const lb=document.getElementById("logudBtn");
        if(lb){lb.hidden=false;lb.onclick=()=>{
            try{localStorage.removeItem("bpa_user");localStorage.removeItem("bpa_btok");localStorage.removeItem("bpa_velkomst_set_"+savedUser.k);}catch(e){}
            try{sessionStorage.removeItem("bpa_adm");}catch(e){}   /* SIKKERHED: log ud = HELT ud — en efterladt admin-session må aldrig arves af næste bruger på maskinen */
            location.reload();
        };}
        const kt=document.getElementById("kalTopBtn");
        if(kt){kt.hidden=false;kt.onclick=()=>{location.hash="#/kalender";};}
        if(!window.__kalTjekKoert){window.__kalTjekKoert=1;setTimeout(function(){try{apiKald({a:"bkaltjek",u:window.BPA_USER});if(typeof bkalFliseOpdater==="function")bkalFliseOpdater();}catch(e){}},1800);}   /* 📅 påmindelses-push + næste-aftale på forsiden */
        /* skift kodeord (kræver serveren — knappen vises altid, dialogen melder pænt fejl lokalt) */
        const kb=document.getElementById("kodeBtn"),ko=document.getElementById("kodeOverlay");
        if(kb&&ko){
            kb.hidden=false;
            const luk=()=>{ko.style.display="none";};
            kb.onclick=()=>{ko.style.display="flex";
                ["skNuv","skNy1","skNy2"].forEach(id=>{document.getElementById(id).value="";});
                document.getElementById("skErr").hidden=true;
                setTimeout(()=>{try{document.getElementById("skNuv").focus();}catch(e){}},80);
            };
            document.getElementById("skLuk").onclick=luk;
            ko.addEventListener("click",e=>{if(e.target===ko)luk();});
            document.getElementById("skGem").onclick=async function(){
                const fe=document.getElementById("skErr");fe.hidden=true;
                const nuv=document.getElementById("skNuv").value,
                    n1=document.getElementById("skNy1").value,
                    n2=document.getElementById("skNy2").value;
                if(n1.length<6){fe.textContent="Det nye kodeord skal være mindst 6 tegn.";fe.hidden=false;return;}
                if(n1!==n2){fe.textContent="De to nye kodeord er ikke ens.";fe.hidden=false;return;}
                this.disabled=true;
                try{
                    const j1=await apiKald({a:"blogin",u:savedUser.k,p:nuv});   /* bekræft nuværende + frisk nøgle */
                    if(!j1||!j1.ok){fe.textContent=(j1&&j1.fejl)||"Forkert nuværende kodeord.";fe.hidden=false;this.disabled=false;return;}
                    try{localStorage.setItem("bpa_btok",j1.token||"");}catch(e){}
                    const j2=await apiKald({a:"bpw",btok:j1.token,ny:n1});
                    if(j2&&j2.ok){luk();alert("Dit kodeord er skiftet ✔");}
                    else{fe.textContent=(j2&&j2.fejl)||"Kunne ikke gemme.";fe.hidden=false;}
                }catch(e){fe.textContent="Kunne ikke nå serveren — kodeordsskift virker kun online.";fe.hidden=false;}
                this.disabled=false;
            };
        }
        efterLogin();return;
    }
    if(admSess&&admSess.token){ /* admin bliver i universet — 🔑 Overblik kommer i menuen */
        gate.remove();window.ER_ADMIN=true;
        /* SIKKERHED: hent hele brugerlisten via det LOGIN-BESKYTTEDE kald (afløser den offentlige
       adgang.js). Lægges i window.ADGANG_TEKST, så admin-UI'et (navne, søgning, rettigheder,
       Ctrl+K) virker som før — men listen er nu kun tilgængelig for indloggede admins. */
        try{apiKald({a:"adgangsliste",token:admSess.token}).then(function(r){
            if(r&&r.ok&&typeof r.tekst==="string"){window.ADGANG_TEKST=r.tekst;
                try{window.__ADGANG_KLAR=1;}catch(e){}
                try{window.dispatchEvent(new HashChangeEvent("hashchange"));}catch(e){}   /* gen-tegn nuværende admin-område med navnene */
            }
        }).catch(function(){});}catch(e){}
        if(!window.BPA_USER)window.BPA_USER=admSess.u;   /* medarbejder-identitet i fx Fællesskabet */
        const lbAdm=document.getElementById("logudBtn");
        if(lbAdm){lbAdm.hidden=false;lbAdm.onclick=()=>{
            /* SIKKERHED: log ud = HELT ud i ét tryk — ryd OGSÅ det huskede borger-login (dobbelt-brugere),
         ellers kræver det 2× log ud, og næste bruger kan arve en halv session */
            try{sessionStorage.removeItem("bpa_adm");}catch(e){}
            try{localStorage.removeItem("bpa_user");localStorage.removeItem("bpa_btok");}catch(e){}
            location.reload();};}
        /* ---------- ADM-TILSTAND: rent adm-univers uden borger-resterne ----------
       Standard for admins. «👁 Se BPA-universet» skifter til borger-visning
       (borgernes univers er 100% uændret — dette gælder KUN admin-sessioner). */
        window.hfSkiftVisning=function(til){
            try{sessionStorage.setItem("adm_visning",til);}catch(e){}
            location.hash=til==="adm"?"#/feedback-admin":"#/";
            location.reload();
        };
        let admVisn="adm";try{admVisn=sessionStorage.getItem("adm_visning")||"adm";}catch(e){}
        if(admVisn==="adm"){
            document.body.classList.add("adm-tilstand");
            /* borger-sider omdirigeres til centralen — adm-sider + Fællesskabet er tilladt */
            const ADM_OK=["feedback-admin","intern","nyheds-admin","faellesskab"];
            const admRute=()=>{
                const h=location.hash.replace(/^#\/?/,"");
                if(ADM_OK.indexOf(h)<0)location.replace("#/feedback-admin");
            };
            window.addEventListener("hashchange",admRute);
            admRute();
            /* topbaren = HF-adm univers: brand + 3 kommunikations-genveje m. rød+antal (CSS skjuler borger-links) */
            const admTit=document.querySelector("header .bar .titel");
            if(admTit)admTit.innerHTML='HF-adm<span>-univers</span>';
            const barH=document.getElementById("barHandling");
            if(barH&&!document.getElementById("hfAdmGenveje")){
                const mkGv=(id,ikon,tekst,omr)=>{
                    const a=document.createElement("a");
                    a.href="#/feedback-admin";a.className="hf-adm-genvej";a.id=id;
                    a.innerHTML=ikon+' '+tekst+'<span class="hf-adm-tal" style="display:none">0</span>';
                    a.onclick=e=>{e.preventDefault();try{sessionStorage.setItem("omr_fokus",omr);}catch(x){}
                        if(location.hash.replace(/^#\/?/,"")==="feedback-admin"){try{window.dispatchEvent(new HashChangeEvent("hashchange"));}catch(x){}}
                        else location.hash="#/feedback-admin";};
                    return a;
                };
                const wrap=document.createElement("span");wrap.id="hfAdmGenveje";
                wrap.style.cssText="display:inline-flex;gap:6px;align-items:center";
                wrap.appendChild(mkGv("hfAdmBesk","💬","Beskeder","support"));
                wrap.appendChild(mkGv("hfAdmFb","📬","Feedback","feedback"));
                wrap.appendChild(mkGv("hfAdmChat","🎧","Live-chat","chat"));
                /* 🎫 Intern support-genvej: går til #/intern på isup-fanen (ikke feedback-admin) */
                (function(){var a=document.createElement("a");a.href="#/intern";a.className="hf-adm-genvej";a.id="hfAdmIsup";a.title="Intern support";
                    a.innerHTML='🎫 Int. support<span class="hf-adm-tal" style="display:none">0</span>';
                    a.onclick=function(e){e.preventDefault();inTilstand="isup";isupSag=null;window.isupFokus="";try{localStorage.setItem("in_mode","isup");}catch(x){}
                        if(location.hash.replace(/^#\/?/,"").split("?")[0]==="intern"){if(typeof renderIntern==="function")renderIntern();}else{location.hash="#/intern";}};
                    wrap.appendChild(a);})();
                barH.appendChild(wrap);
            }
            hfAdmTael();
            if(!window._hfAdmTaelTimer){window._hfAdmTaelTimer=setInterval(hfAdmTael,15000);
                document.addEventListener("visibilitychange",()=>{if(!document.hidden)hfAdmTael();});}
        }else{
            /* borger-visning (admin-session): navigation til et rent adm-område — fx via den mørke
         bjælkes Admin-central/Intern/Nyheder-links (hash-skift uden reload) — skal automatisk
         skifte tilbage til adm-tilstand, ellers bliver borger-toplinjen stående i adm-universet.
         Fællesskabet er bevidst IKKE med (findes i begge visninger). KUN admin-sessioner rammer
         denne kode — borgernes univers er 100% uændret. */
            const ADM_SIDER=["feedback-admin","intern","nyheds-admin"];
            const admAutoRetur=()=>{
                const h=location.hash.replace(/^#\/?/,"");
                if(ADM_SIDER.indexOf(h)>=0){
                    try{sessionStorage.setItem("adm_visning","adm");}catch(e){}
                    location.reload();
                }
            };
            window.addEventListener("hashchange",admAutoRetur);
            admAutoRetur();   /* fanger også direkte load på #/feedback-admin i borger-visning */
        }
        efterLogin();return;
    }
    gate.style.display="flex";
    const inp=document.getElementById("gateInput"),err=document.getElementById("gateErr"),
        pw=document.getElementById("gatePw"),btn=document.getElementById("gateBtn");
    const ERR_STD=err.textContent;
    function visFejl(t){err.textContent=t;err.hidden=false;}
    /* tvunget kodeordsskift (første login / efter nulstilling) */
    function visSkiftKode(u,btok){
        const kort=gate.querySelector(".gate-kort");
        kort.innerHTML='<img src="https://handicapformidlingen.dk/wp-content/uploads/hf_logo_www_header.png" alt="Handicapformidlingen">'+
            '<h1>Vælg dit eget kodeord</h1>'+
            '<p>Af hensyn til din sikkerhed skal du vælge et personligt kodeord (mindst 6 tegn), før du fortsætter.</p>'+
            '<input id="nk1" type="password" autocomplete="new-password" placeholder="Nyt kodeord (mindst 6 tegn)" aria-label="Nyt kodeord">'+
            '<input id="nk2" type="password" autocomplete="new-password" placeholder="Gentag det nye kodeord" aria-label="Gentag kodeord">'+
            '<div id="nkErr" style="color:var(--roed);font-size:.9rem;margin-top:10px" hidden></div>'+
            '<button id="nkBtn">Gem kodeord og fortsæt →</button>';
        const k1=document.getElementById("nk1"),k2=document.getElementById("nk2"),
            ne=document.getElementById("nkErr"),nb=document.getElementById("nkBtn");
        nb.style.cssText=btn.style.cssText;
        async function gem(){
            ne.hidden=true;
            const ny=k1.value;
            if(ny.length<6){ne.textContent="Kodeordet skal være mindst 6 tegn.";ne.hidden=false;return;}
            if(ny!==k2.value){ne.textContent="De to kodeord er ikke ens — prøv igen.";ne.hidden=false;return;}
            nb.disabled=true;
            try{
                const j=await apiKald({a:"bpw",btok:btok,ny:ny});
                if(j&&j.ok){
                    try{localStorage.setItem("bpa_user",u);}catch(e){}
                    location.reload();return;
                }
                ne.textContent=(j&&j.fejl)||"Kunne ikke gemme — prøv igen.";ne.hidden=false;
            }catch(e){ne.textContent="Kunne ikke gemme — tjek forbindelsen.";ne.hidden=false;}
            nb.disabled=false;
        }
        nb.onclick=gem;
        k1.addEventListener("keydown",e=>{if(e.key==="Enter")k2.focus();});
        k2.addEventListener("keydown",e=>{if(e.key==="Enter")gem();});
        setTimeout(()=>{try{k1.focus();}catch(e){}},100);
    }
    /* tvungent admin-kodeordsskift: startkoden virker KUN til at vælge sit eget — man
     kommer ikke ind, før det er gjort (politik: min. 8 tegn m. bogstav, tal og specialtegn) */
    function visAdmSkift(v,nuvP,token,erBorger){
        const ov=document.createElement("div");
        ov.style.cssText="position:fixed;inset:0;z-index:1001;background:rgba(30,55,76,.55);display:flex;align-items:center;justify-content:center;padding:18px";   /* 1001: SKAL ligge over #gate (z-index:1000), ellers er skift-dialogen usynlig og login "hænger" */
        ov.innerHTML='<div style="width:min(430px,100%);background:#fff;border-radius:22px;padding:28px;font-family:var(--font);color:var(--moerk)">'+
            '<h3 style="margin:0 0 6px;font-size:1.1rem">🔒 Vælg dit eget admin-kodeord</h3>'+
            '<p style="color:var(--graa);font-size:.85rem;margin:0 0 14px">Startkodeordet skal skiftes, før du kan komme ind. Krav: mindst 8 tegn med bogstav, tal og specialtegn.</p>'+
            '<input id="asNy1" type="password" autocomplete="new-password" placeholder="Nyt kodeord" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:11px 14px;font-family:var(--font);margin-bottom:8px;outline:none">'+
            '<input id="asNy2" type="password" autocomplete="new-password" placeholder="Gentag det nye kodeord" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:11px 14px;font-family:var(--font);outline:none">'+
            '<div id="asFejl" style="color:var(--roed);font-size:.85rem;margin-top:8px;display:none"></div>'+
            '<button id="asGem" style="width:100%;margin-top:14px;border:0;border-radius:12px;padding:13px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.95rem;color:#fff;background:linear-gradient(120deg,var(--moerk),#2a5573)">Gem og log ind →</button></div>';
        document.body.appendChild(ov);
        const politik=k=>k.length>=8&&/[A-Za-zÆØÅæøå]/.test(k)&&/\d/.test(k)&&/[^A-Za-z0-9ÆØÅæøå]/.test(k);
        const fejlEl=ov.querySelector("#asFejl");
        async function gem(){
            const n1=ov.querySelector("#asNy1").value,n2=ov.querySelector("#asNy2").value;
            fejlEl.style.display="none";
            if(!politik(n1)){fejlEl.textContent="Kodeordet skal være mindst 8 tegn og indeholde bogstav, tal og specialtegn.";fejlEl.style.display="block";return;}
            if(n1!==n2){fejlEl.textContent="De to kodeord er ikke ens.";fejlEl.style.display="block";return;}
            const knap=ov.querySelector("#asGem");knap.disabled=true;
            try{
                const r=await apiKald({a:"admegenkode",token:token,nuv:nuvP,nyt:n1});
                if(r&&r.ok){
                    try{sessionStorage.setItem("bpa_adm",JSON.stringify({u:v,token:token}));}catch(e){}
                    if(erBorger){try{localStorage.setItem("bpa_user",v);localStorage.setItem("bpa_navn",v);}catch(e){}}
                    location.reload();return;
                }
                fejlEl.textContent=(r&&r.fejl)||"Kunne ikke gemme — prøv igen.";fejlEl.style.display="block";knap.disabled=false;
            }catch(e){fejlEl.textContent="Kunne ikke gemme — tjek forbindelsen.";fejlEl.style.display="block";knap.disabled=false;}
        }
        ov.querySelector("#asGem").onclick=gem;
        ov.querySelector("#asNy2").addEventListener("keydown",e=>{if(e.key==="Enter")gem();});
        setTimeout(()=>{try{ov.querySelector("#asNy1").focus();}catch(e){}},100);
    }
    async function tryLogin(){
        const v=norm(inp.value),p=pw.value;
        err.hidden=true;
        if(!v){try{inp.focus();}catch(e){}return;}
        btn.disabled=true;
        /* 1) ADMINISTRATOR er den PRIMÆRE indgang for administratorer.
          admcheck fortæller server-side om v er admin og/eller borger — UDEN en offentlig liste
          (rene borgere er ikke admins → svarer hurtigt nej). */
        let erAdm=false,erBorgerBruger=false;
        try{const ja=await apiKald({a:"admcheck",u:v});erAdm=(ja&&ja.adm===true);erBorgerBruger=(ja&&ja.borger===true);}catch(e){}
        if(erAdm){
            let j3=null;
            try{j3=await apiKald({a:"admlogin",u:v,p:p});}catch(e){}
            if(j3&&j3.ok&&j3.token){
                if(j3.skift){visAdmSkift(v,p,j3.token,erBorgerBruger);return;}   /* startkode → SKAL skiftes før man kommer ind */
                try{sessionStorage.setItem("bpa_adm",JSON.stringify({u:v,token:j3.token}));}catch(e){}
                if(erBorgerBruger){try{localStorage.setItem("bpa_user",v);localStorage.setItem("bpa_navn",v);}catch(e){}}   /* har de også borger-adgang, følger borger-visningen med */
                location.reload();return;
            }
            /* admin-login mislykkedes. Er brugeren IKKE også borger, er det bare forkert admin-kodeord. */
            if(!erBorgerBruger){
                btn.disabled=false;
                visFejl((j3&&j3.fejl)||"Du er oprettet som ADMINISTRATOR — brug dit admin-kodeord (ikke et evt. gammelt borger-kodeord). Første gang: det fælles startkodeord, hvorefter du vælger dit eget. I tvivl? Spørg Søren.");
                try{pw.select();}catch(e){}return;
            }
            /* ellers: måske skrev de deres BORGER-kodeord — fald igennem til borger-login nedenfor */
        }
        /* 2) BORGER-login (rene borgere + interne der bruger deres borger-kodeord) */
        if(erBorgerBruger){
            let j=null,offline=false;
            try{j=await apiKald({a:"blogin",u:v,p:p});}catch(e){offline=true;}
            btn.disabled=false;
            /* SIKKERHED: en REN borger (ikke admin) dræber enhver efterladt admin-session i fanen —
         borgere må ALDRIG lande i adm-universet, uanset hvem der var logget ind før */
            if(!erAdm){try{sessionStorage.removeItem("bpa_adm");}catch(e){}}
            if(offline){ /* lokal fil-test uden PHP: luk ind uden kodeordstjek */
                try{localStorage.setItem("bpa_user",v);}catch(e){}
                location.reload();return;
            }
            if(!j||!j.ok){
                visFejl(erAdm?"Forkert kodeord. Er du administrator, så brug dit ADMIN-kodeord."
                    :((j&&j.fejl)||"Kunne ikke logge ind — prøv igen."));
                try{pw.select();}catch(e){}return;
            }
            try{localStorage.setItem("bpa_btok",j.token||"");localStorage.setItem("bpa_navn",j.navn||"");}catch(e){}
            if(j.skift){visSkiftKode(v,j.token);return;}
            try{localStorage.setItem("bpa_user",v);}catch(e){}
            location.reload();return;
        }
        /* 3) hverken administrator eller borger */
        btn.disabled=false;
        visFejl(ERR_STD);try{inp.select();}catch(e){}
    }
    btn.onclick=tryLogin;
    inp.addEventListener("keydown",e=>{if(e.key==="Enter")pw.focus();});
    pw.addEventListener("keydown",e=>{if(e.key==="Enter")tryLogin();});
    setTimeout(()=>{try{inp.focus();}catch(e){}},250);
})();

/* ===================== Efter login: hilsen, nyheder, reveal ===================== */
function efterLogin(){
    /* personlig velkomst med borgerens fornavn (fra adgang.js-listen) */
    const navn=(window.BPA_NAME||"").split(" ")[0];
    document.getElementById("heroHej").innerHTML=navn
        ? "Velkommen <em>"+navn+"</em> i dit <em>BPA-univers</em>"
        : "Velkommen i dit <em>BPA-univers</em>";
    visVelkomstvideo();
    renderNyheder();
}

/* ===================== Velkomstvideo (hero) ===================== */
function visVelkomstvideo(){
    const boks=document.getElementById("heroVid"),v=document.getElementById("uniVid"),knap=document.getElementById("vidStart");
    if(!boks||!v)return;
    /* vis kun hvis filen faktisk ligger på serveren (Range-tjek — HEAD drilller på Simply) */
    fetch("velkomst-univers.mp4",{headers:{Range:"bytes=0-1"}}).then(r=>{
        if(!(r.ok||r.status===206))return;
        boks.hidden=false;
        knap.onclick=()=>{knap.hidden=true;v.play().catch(()=>{});};
        v.addEventListener("play",()=>{knap.hidden=true;});
        /* egne undertekster (velkomst-tekster.js) i fast boks + til/fra-knap */
        const tBoks=document.getElementById("vidTekst"),tInd=document.getElementById("vidTekstInd"),
            tKnap=document.getElementById("vidTekstKnap");
        const cues=Array.isArray(window.VELKOMST_TEKSTER)?window.VELKOMST_TEKSTER:[];
        let tekstTil=true;try{tekstTil=localStorage.getItem("bpa_vidtekst")!=="fra";}catch(e){}
        function tKnapTekst(){tKnap.textContent=tekstTil?"💬 Tekst: til":"💬 Tekst: fra";}
        tKnapTekst();
        let sidste=null;
        function visTekst(){
            if(!tekstTil||!cues.length){tBoks.hidden=true;return;}
            const t=v.currentTime;
            const cue=cues.find(c=>t>=c[0]&&t<c[1]);
            if(cue){if(cue!==sidste){tInd.textContent=cue[2];sidste=cue;}tBoks.hidden=false;}
            else{tBoks.hidden=true;sidste=null;}
        }
        v.addEventListener("timeupdate",visTekst);
        v.addEventListener("seeked",visTekst);
        tKnap.onclick=()=>{tekstTil=!tekstTil;
            try{localStorage.setItem("bpa_vidtekst",tekstTil?"til":"fra");}catch(e){}
            tKnapTekst();visTekst();};
        /* første login: afspil automatisk MED lyd — blokerer browseren, vises en stor ▶-knap i stedet */
        const u=(window.BPA_USER||"").trim();
        if(!u||u==="faelles")return;
        const KEY="bpa_velkomst_set_"+u;
        let set=false;try{set=!!localStorage.getItem(KEY);}catch(e){}
        if(set)return;
        try{localStorage.setItem(KEY,"1");}catch(e){}
        const p=v.play();                        /* unmuted — som ønsket */
        if(p&&p.catch)p.catch(()=>{knap.hidden=false;});
    }).catch(()=>{});
}

/* ===================== Nyheder (server-styret; nyheder.js = reserve ved lokal test) ===================== */
const NYKAT={nyt:["📢","Nyt fra HF"],system:["🔧","Systemopdateringer"],drift:["⚠️","Driftsinfo"],arrangement:["🎪","Arrangementer"],tip:["💡","Tips & tricks"]};
let nyData=null;                 /* {nyheder:[], laest:{}} fra serveren */
const nyUdvid=new Set();         /* kategorier hvor «Vis alle» er trykket */
function nyBruger(){try{return (localStorage.getItem("bpa_user")||"").trim().toLowerCase();}catch(e){return "";}}
function nyKortHTML(n,iArkiv){
    const kat=NYKAT[n.kat]||["📢","Nyt"];
    const d=new Date((n.ts||0)*1000),p=x=>(x<10?"0":"")+x;
    const dato=p(d.getDate())+"."+p(d.getMonth()+1)+"."+d.getFullYear();
    return '<div class="n-kort"><div class="n-top"><span class="n-tag '+fbaEsc(n.kat||"nyt")+'">'+kat[0]+' '+kat[1]+'</span>'+
        '<span class="n-dato">'+dato+'</span>'+
        (iArkiv?'':(nyBruger()?'<button class="n-luk" data-id="'+fbaEsc(n.id)+'" title="Markér som læst og læg i arkiv">✓ Luk</button>':''))+
        '</div><h4>'+fbaEsc(n.titel||"")+'</h4><p>'+fbaEsc(n.tekst||"")+'</p>'+
        (n.link?'<p style="margin-top:8px"><a href="'+fbaEsc(n.link)+'" target="_blank" rel="noopener">'+(n.linktxt==="tryk"?"Tryk her":"Læs mere")+' →</a></p>':"")+'</div>';
}
async function hentNyheder(){
    try{
        const j=await apiKald({a:"nyheder",u:nyBruger()});
        if(j&&j.ok){nyData={nyheder:j.nyheder||[],laest:j.laest||{}};}
    }catch(e){ /* lokal test uden PHP → nyheder.js */
        nyData={nyheder:(Array.isArray(window.NYHEDER)?window.NYHEDER:[]).map((n,i)=>({id:"lokal"+i,kat:n.type==="drift"?"drift":n.type==="arrangement"?"arrangement":"nyt",titel:n.titel,tekst:n.tekst,link:n.link,ts:0,status:"aktiv",_dato:n.dato})),laest:{}};
    }
    renderNyhederNy();visNytBanner();
}
function nyDel(){
    const alle=(nyData&&nyData.nyheder)||[],laest=(nyData&&nyData.laest)||{};
    const nye=alle.filter(n=>(n.status||"aktiv")==="aktiv"&&!laest[n.id]);
    const arkiv=alle.filter(n=>(n.status||"aktiv")==="arkiv"||laest[n.id]);
    return {nye,arkiv};
}
function renderNyhederNy(){
    const liste=document.getElementById("nyhedsListe");if(!liste||!nyData)return;
    const {nye,arkiv}=nyDel();
    let html="";
    Object.keys(NYKAT).forEach(kat=>{
        const g=nye.filter(n=>(n.kat||"nyt")===kat).sort((a,b)=>(b.ts||0)-(a.ts||0));
        if(!g.length)return;
        const vis=nyUdvid.has(kat)?g:g.slice(0,4);
        html+='<h3 class="n-kat">'+NYKAT[kat][0]+' '+NYKAT[kat][1]+'</h3>'+vis.map(n=>nyKortHTML(n,false)).join("")+
            (g.length>4&&!nyUdvid.has(kat)?'<button class="n-flere" data-kat="'+kat+'">Vis alle '+g.length+' →</button>':'');
    });
    liste.innerHTML=html||'<p style="color:var(--graa)">Ingen nye nyheder — du er helt opdateret 🎉</p>';
    liste.querySelectorAll(".n-flere").forEach(b=>{b.onclick=()=>{nyUdvid.add(b.getAttribute("data-kat"));renderNyhederNy();};});
    liste.querySelectorAll(".n-luk").forEach(b=>{
        b.onclick=async function(){
            const id=this.getAttribute("data-id");this.disabled=true;
            try{
                const j=await apiKald({a:"nylaest",u:nyBruger(),id:id});
                if(j&&j.ok){nyData.laest[id]=1;renderNyhederNy();visNytBanner();}
                else this.disabled=false;
            }catch(e){this.disabled=false;}
        };
    });
    /* arkivet */
    const boks=document.getElementById("nyArkivBoks");
    if(boks){
        const ark=arkiv.sort((a,b)=>(b.ts||0)-(a.ts||0));
        boks.hidden=!ark.length;
        document.getElementById("nyArkivAntal").textContent=ark.length;
        document.getElementById("nyArkivListe").innerHTML=ark.map(n=>nyKortHTML(n,true)).join("");
    }
    /* teaser på forsiden: seneste 2 nye */
    const t=document.getElementById("nyhedsTeaser");
    if(t){
        const top=nye.sort((a,b)=>(b.ts||0)-(a.ts||0)).slice(0,2);
        t.innerHTML=top.length?'<h2>📣 Seneste nyt</h2><div style="margin-top:18px">'+top.map(n=>nyKortHTML(n,true)).join("")+'</div>'+
            '<p><a class="knap" href="#/nyheder">Se alle nyheder</a></p>':"";
    }
    sætReveal();
}
/* diskret banner under toppen: «n nye nyheder» */
function visNytBanner(){
    const b=document.getElementById("nytBanner");if(!b||!nyData)return;
    const {nye}=nyDel();
    const nyeste=nye.reduce((m,n)=>Math.max(m,n.ts||0),0);
    let lukket=0;try{lukket=parseInt(sessionStorage.getItem("nyt_banner")||"0",10);}catch(e){}
    if(nye.length&&nyeste>lukket){
        b.innerHTML='📣 <b>'+nye.length+' '+(nye.length===1?'ny nyhed':'nye nyheder')+'</b> venter på dig'+
            '<a href="#/nyheder">Læs dem →</a><button id="nytBannerLuk" aria-label="Skjul">✕</button>';
        b.hidden=false;
        document.getElementById("nytBannerLuk").onclick=()=>{b.hidden=true;try{sessionStorage.setItem("nyt_banner",String(nyeste));}catch(e){}};
    }else b.hidden=true;
}
function renderNyheder(){hentNyheder();}   /* bagudkompatibelt kald fra efterLogin */

/* ===================== 🆘 Support (borger) ===================== */
const SUP_RUTER=[
    {v:"kons_min",ikon:"👤",navn:"Min konsulent",besk:"Din egen faste konsulent"},
    {v:"kons_team",ikon:"🤝",navn:"En BPA-konsulent",besk:"Konsulentteamet — behøver ikke være din egen"},
    {v:"loen",ikon:"💰",navn:"Løn",besk:"Spørgsmål om løn"},
    {v:"hr",ikon:"🧑‍💼",navn:"HR",besk:"Ansættelse, personale m.m."},
    {v:"generel",ikon:"🏢",navn:"Handicapformidlingen",besk:"Noget alment / andet"}
];
var supSag=null;
async function renderSupport(){
    const el=document.getElementById("supIndhold");if(!el)return;
    if(!window.BPA_USER||window.BPA_USER==="faelles"){el.innerHTML='<p style="color:var(--graa)">Log ind for at bruge support.</p>';return;}
    if(window.__supNyStart){window.__supNyStart=0;supNyForm();return;}   /* kom fra «Skriv til din konsulent» → åbn ny henvendelse direkte */
    if(supSag){supVisSag(supSag);return;}
    el.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    let j=null;try{j=await apiKald({a:"smine",u:window.BPA_USER});}catch(e){}
    if(!(j&&j.ok)){el.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — tjek forbindelsen.</p>';return;}
    const sager=j.sager||[];
    let h='<div id="supPushBar"></div><button id="supNyKnap" class="knap groen" style="margin:0 0 16px">➕ Ny henvendelse</button>';
    if(!sager.length)h+='<p style="color:var(--graa)">Du har ingen support-sager endnu. Opret en ovenfor 👆</p>';
    else h+=sager.map(s=>'<div class="fba-kort sup-sag" data-sid="'+fbaEsc(s.sid)+'" style="cursor:pointer'+(s.nyt&&!s.lukket?';border-left:5px solid var(--roed)':'')+'">'+
        '<div class="fba-top"><span style="font-weight:800">'+(s.nyt&&!s.lukket?'🔴 ':'')+fbaEsc(s.emne)+'</span>'+
        '<span style="color:var(--graa);font-size:.8rem">'+fbaEsc(s.ruteIkon+" "+s.ruteNavn)+'</span>'+
        '<span style="margin-left:auto;font-size:.78rem;font-weight:700;color:'+(s.besvaret?'#41630a':'var(--graa)')+'">'+(s.lukket?'✔ Lukket':(s.besvaret?'💬 Besvaret':'⏳ Afventer svar'))+'</span></div>'+
        '<div style="color:var(--graa);font-size:.78rem;margin-top:2px">'+fbaDato(s.sidst)+'</div></div>').join("");
    el.innerHTML=h;
    const nk=document.getElementById("supNyKnap");if(nk)nk.onclick=supNyForm;
    el.querySelectorAll(".sup-sag").forEach(k=>k.onclick=()=>{supSag=k.getAttribute("data-sid");renderSupport();});
    supPushBar();
}
async function supPushBar(){
    const el=document.getElementById("supPushBar");if(!el)return;
    const iOS=/iphone|ipad|ipod/i.test(navigator.userAgent);
    const standalone=(window.matchMedia&&window.matchMedia('(display-mode: standalone)').matches)||window.navigator.standalone===true;
    if(!('serviceWorker' in navigator)||!('PushManager' in window)||!('Notification' in window)){
        el.innerHTML=(iOS&&!standalone)?'<div class="fba-kort" style="border-left:5px solid var(--blaa);font-size:.83rem;color:var(--graa)">🔔 Vil du have besked, når vi svarer? Læg universet på hjemmeskærmen (Del → «Føj til hjemmeskærm»), åbn fra ikonet, og slå så til her (kræver iOS 16.4+).</div>':'';
        return;
    }
    if(Notification.permission==='denied'){el.innerHTML='';return;}
    let sub=null;try{const reg=await navigator.serviceWorker.ready;sub=await reg.pushManager.getSubscription();}catch(e){}
    el.innerHTML='<div class="fba-kort" style="border-left:5px solid var(--blaa);display:flex;align-items:center;gap:10px;flex-wrap:wrap"><b>🔔 Besked ved svar</b>'+
        (sub?'<span style="color:#41630a;font-weight:700;font-size:.85rem">✅ Slået til</span><span style="flex:1"></span><button id="supPushFra" style="border:0;background:#eef3f7;border-radius:10px;padding:8px 14px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.82rem">Slå fra</button>'
            :'<span style="color:var(--graa);font-size:.85rem;flex:1;min-width:120px">Få en notifikation, når vi svarer — også når appen er lukket.</span><button id="supPushTil" style="border:0;background:var(--moerk);color:#fff;border-radius:10px;padding:8px 14px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.82rem">Slå til</button>')+'</div>';
    const til=document.getElementById("supPushTil");if(til)til.onclick=supPushTilmeld;
    const fra=document.getElementById("supPushFra");if(fra)fra.onclick=async()=>{try{const reg=await navigator.serviceWorker.ready;const s=await reg.pushManager.getSubscription();if(s){await apiKald({a:"bpushunsub",u:window.BPA_USER,endpoint:s.endpoint});await s.unsubscribe();}}catch(e){}supPushBar();};
}
async function supPushTilmeld(){
    try{const perm=await Notification.requestPermission();if(perm!=="granted"){alert("Du sagde nej til notifikationer. Du kan slå dem til i browserindstillinger.");return;}
        const reg=await navigator.serviceWorker.ready;
        const j=await apiKald({a:"pushpub"});if(!(j&&j.ok&&j.public)){alert("Notifikationer er ikke sat op på serveren endnu.");return;}
        const s=await reg.pushManager.subscribe({userVisibleOnly:true,applicationServerKey:pushB64(j.public)});
        await apiKald({a:"bpushsub",u:window.BPA_USER,sub:s.toJSON()});
        supPushBar();
    }catch(e){alert("Kunne ikke slå til: "+(e&&e.message?e.message:e));}
}
/* ---- vedhæftninger (delt mellem borger + adm) ---- */
const SUP_FIL_ACCEPT=".jpg,.jpeg,.png,.gif,.webp,.heic,.pdf,.txt,.csv,.doc,.docx,.xls,.xlsx,image/*";
/* ===== billede-lightbox (åbner billeder i overlay i stedet for ny fane) — delt af Beskeder + kanaler ===== */
function hfLightbox(url){
    var ov=document.createElement("div");
    ov.className="hf-lb-ov";
    ov.style.cssText="position:fixed;inset:0;z-index:9998;background:rgba(15,25,35,.85);display:flex;align-items:center;justify-content:center;padding:20px;cursor:zoom-out";
    ov.innerHTML='<img src="'+fbaEsc(url)+'" alt="" style="max-width:96vw;max-height:92vh;border-radius:12px;box-shadow:0 20px 70px rgba(0,0,0,.6)">'+
        '<button aria-label="Luk" style="position:fixed;top:14px;right:18px;border:0;background:rgba(255,255,255,.18);color:#fff;width:40px;height:40px;border-radius:50%;font-size:1.2rem;cursor:pointer">✕</button>';
    ov.addEventListener("click",function(){ov.remove();});
    document.body.appendChild(ov);
}
if(!window.__hfLbInit){window.__hfLbInit=1;
    document.addEventListener("click",function(e){var im=e.target.closest&&e.target.closest(".hf-lb");if(im){e.preventDefault();e.stopPropagation();hfLightbox(im.getAttribute("data-url"));}});
    document.addEventListener("keydown",function(e){if(e.key==="Escape"){document.querySelectorAll(".hf-lb-ov").forEach(function(o){o.remove();});}});
}
function supFilIkon(n){const e=((n||"").split(".").pop()||"").toLowerCase();if(e==="pdf")return"📕";if(e==="doc"||e==="docx")return"📄";if(e==="xls"||e==="xlsx"||e==="csv")return"📊";if(e==="txt")return"📃";return"📎";}
function supFilerHTML(filer,mig){
    if(!filer||!filer.length)return"";
    /* SIKKERHED: vedhæftninger kræver nu login — send sessionens token med (admin-token el. borger-btok) */
    var _s=null;try{_s=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
    var _ft=(_s&&_s.token)||"";try{if(!_ft)_ft=localStorage.getItem("bpa_btok")||"";}catch(e){}
    var _tq=_ft?("&t="+encodeURIComponent(_ft)):"";
    return '<div style="display:flex;flex-wrap:wrap;gap:6px;margin-top:7px">'+filer.map(f=>{
        const url="mithf-api.php?a=supfil&f="+encodeURIComponent(f.navn)+_tq;
        if(f.billede)return '<img class="hf-lb" data-url="'+url+'" src="'+url+'" alt="'+fbaEsc(f.orig||"billede")+'" style="max-width:160px;max-height:160px;border-radius:10px;display:block;cursor:zoom-in">';
        return '<a href="'+url+'" download="'+fbaEsc(f.orig||"fil")+'" rel="noopener" style="display:inline-flex;align-items:center;gap:6px;background:'+(mig?'rgba(255,255,255,.18)':'#eef3f7')+';color:'+(mig?'#fff':'var(--moerk)')+';padding:7px 11px;border-radius:9px;font-size:.8rem;font-weight:700;text-decoration:none;max-width:210px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="Hent '+fbaEsc(f.orig||"fil")+'">'+supFilIkon(f.orig||f.navn)+' '+fbaEsc(f.orig||"fil")+'</a>';
    }).join("")+'</div>';
}
function supUploader(listeEl,auth){
    const s={filer:[]};
    s.vaelg=function(){
        const inp=document.createElement("input");inp.type="file";inp.accept=SUP_FIL_ACCEPT;inp.multiple=true;
        inp.onchange=async()=>{
            for(const file of Array.from(inp.files||[])){
                if(s.filer.length>=8){alert("Du kan vedhæfte op til 8 filer.");break;}
                if(file.size>8*1024*1024){alert(file.name+": filen er for stor (maks 8 MB).");continue;}
                let b64;try{b64=await new Promise((res,rej)=>{const fr=new FileReader();fr.onload=()=>res(fr.result);fr.onerror=rej;fr.readAsDataURL(file);});}catch(e){continue;}
                const body=Object.assign({a:"supupload",navn:file.name,data:b64},auth||{});
                let r=null;try{r=await apiKald(body);}catch(e){}
                if(r&&r.ok){s.filer.push({navn:r.navn,orig:r.orig,billede:r.billede});s.tegn();}
                else{alert((r&&r.fejl)||(file.name+": kunne ikke uploades."));}
            }
        };
        inp.click();
    };
    s.tegn=function(){
        if(!listeEl)return;
        listeEl.innerHTML=s.filer.map((f,i)=>'<span style="display:inline-flex;align-items:center;gap:5px;background:#eef3f7;border-radius:8px;padding:4px 9px;font-size:.78rem;font-weight:700;margin:4px 5px 0 0">'+(f.billede?"🖼️":supFilIkon(f.orig||f.navn))+' '+fbaEsc((f.orig||"fil").slice(0,26))+' <b data-i="'+i+'" style="cursor:pointer;color:var(--roed)">✕</b></span>').join("");
        listeEl.querySelectorAll("b[data-i]").forEach(x=>x.onclick=()=>{s.filer.splice(+x.getAttribute("data-i"),1);s.tegn();});
    };
    return s;
}
function supNyForm(){
    const el=document.getElementById("supIndhold");if(!el)return;
    el.innerHTML='<button id="supRetur" class="knap" style="background:#eef3f7;color:var(--moerk);margin:0 0 14px">‹ Tilbage</button>'+
        '<div class="fba-kort"><b style="font-size:1.05rem">✍️ Ny henvendelse</b>'+
        '<div style="margin-top:10px"><label style="font-weight:700;font-size:.85rem;color:var(--graa)">Hvem skal den til?</label>'+
        '<div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:6px">'+
        SUP_RUTER.map((r,i)=>'<button class="sup-rute" data-v="'+r.v+'" title="'+fbaEsc(r.besk)+'" style="border:2px solid '+(i===0?'var(--blaa)':'#dbe6ee')+';background:'+(i===0?'rgba(60,180,229,.1)':'#fff')+';border-radius:12px;padding:8px 12px;cursor:pointer;font-family:var(--font);font-size:.85rem;font-weight:700">'+r.ikon+' '+fbaEsc(r.navn)+'</button>').join("")+'</div></div>'+
        '<input id="supEmne" maxlength="140" placeholder="Emne (kort)" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:11px 14px;font-family:var(--font);font-size:.95rem;margin-top:12px">'+
        '<textarea id="supTxt" rows="5" maxlength="4000" placeholder="Skriv din henvendelse …" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:11px 14px;font-family:var(--font);font-size:.95rem;margin-top:10px"></textarea>'+
        '<div id="supNyFiler" style="margin-top:2px"></div>'+
        '<div style="display:flex;gap:10px;align-items:center;margin-top:12px;flex-wrap:wrap"><button id="supSend" class="knap groen">Send henvendelsen</button><button id="supVedh" type="button" class="knap" style="background:#eef3f7;color:var(--moerk)">📎 Vedhæft</button>'+hfEmojiKnap("supTxt")+'<span id="supFejl" style="color:var(--roed);font-size:.85rem"></span></div></div>';
    document.getElementById("supRetur").onclick=()=>{supSag=null;renderSupport();};
    let valgt="kons_min";
    el.querySelectorAll(".sup-rute").forEach(b=>b.onclick=()=>{valgt=b.getAttribute("data-v");el.querySelectorAll(".sup-rute").forEach(x=>{const on=x===b;x.style.borderColor=on?"var(--blaa)":"#dbe6ee";x.style.background=on?"rgba(60,180,229,.1)":"#fff";});});
    const up=supUploader(document.getElementById("supNyFiler"),{u:window.BPA_USER});
    document.getElementById("supVedh").onclick=()=>up.vaelg();
    document.getElementById("supSend").onclick=async function(){
        const emne=document.getElementById("supEmne").value.trim(),txt=document.getElementById("supTxt").value.trim(),fe=document.getElementById("supFejl");
        if(!emne||!txt){fe.textContent="Udfyld både emne og besked.";return;}
        this.disabled=true;fe.textContent="";
        try{const r=await apiKald({a:"sopret",u:window.BPA_USER,n:window.BPA_NAME||"",rute:valgt,emne:emne,txt:txt,filer:up.filer});
            if(r&&r.ok){supSag=r.sid;renderSupport();}else{fe.textContent=(r&&r.fejl)||"Kunne ikke sende.";this.disabled=false;}}
        catch(e){fe.textContent="Kunne ikke sende — tjek forbindelsen.";this.disabled=false;}
    };
}
async function supVisSag(sid){
    const el=document.getElementById("supIndhold");if(!el)return;
    let j=null;try{j=await apiKald({a:"shent",u:window.BPA_USER,sid:sid});}catch(e){}
    if(!(j&&j.ok)){el.innerHTML='<p style="color:var(--roed)">Kunne ikke hente sagen. <button class="knap" style="background:#eef3f7;color:var(--moerk);padding:6px 14px" onclick="supSag=null;renderSupport()">Tilbage</button></p>';return;}
    const bobler=(j.beskeder||[]).map(b=>{
        if(b.fra==="system")return '<div style="text-align:center;color:var(--graa);font-size:.78rem;margin:6px 0">'+fbaEsc(b.txt)+'</div>';
        const navn=b.afVis||b.af||(b.fra==="adm"?"Handicapformidlingen":"Dig");
        return '<div style="display:flex;gap:10px;margin:0 0 14px">'+
            hfAvatarHTML(b.af,navn)+
            '<div style="flex:1;min-width:0">'+
            '<div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(navn)+'</b> · '+fbaDato(b.ts)+'</div>'+
            (b.txt?'<div style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+fbaEsc(b.txt)+'</div>':'')+
            supFilerHTML(b.filer,false)+
            '</div></div>';
    }).join("");
    el.innerHTML='<button id="supRetur" class="knap" style="background:#eef3f7;color:var(--moerk);margin:0 0 14px">‹ Mine sager</button>'+
        '<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="padding:14px 18px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff"><b>'+fbaEsc(j.emne)+'</b>'+
        '<div style="opacity:.8;font-size:.78rem">'+(j.status==="besvaret"?"💬 Besvaret":(j.status==="taget"?"👤 En medarbejder ser på den":"⏳ Afventer svar"))+'</div></div>'+
        '<div style="display:flex;flex-direction:column;gap:2px;padding:16px;background:var(--flade);max-height:52vh;overflow-y:auto">'+bobler+'</div>'+
        (j.borgerLukket?'<div style="padding:12px 16px;text-align:center;color:var(--graa);font-size:.85rem">Du har lukket denne sag. <button id="supGenaaben" class="knap" style="background:#eef3f7;color:var(--moerk);padding:6px 14px;font-size:.85rem">Genåbn</button></div>':
            '<div style="padding:12px 16px;background:#fff;border-top:1px solid rgba(30,55,76,.08)"><div style="display:flex;gap:8px;align-items:center"><button id="supSvarVedh" type="button" title="Vedhæft billede eller fil" style="border:2px solid #dbe6ee;background:#fff;border-radius:999px;width:42px;height:42px;font-size:1.1rem;cursor:pointer;flex:0 0 auto">📎</button><input id="supSvar" maxlength="4000" placeholder="Skriv et svar …" style="flex:1;border:2px solid #dbe6ee;border-radius:999px;padding:10px 15px;font-family:var(--font);font-size:.92rem">'+hfEmojiKnap("supSvar")+'<button id="supSvarSend" class="knap groen" style="padding:10px 16px">Send</button></div>'+
            '<div id="supSvarFiler"></div>'+
            '<button id="supLuk" style="border:0;background:none;color:var(--graa);font-size:.82rem;font-weight:700;cursor:pointer;margin-top:8px;font-family:var(--font)">✔ Luk sagen (jeg har fået den hjælp, jeg skulle bruge)</button></div>')+
        '</div>';
    hfAvatarWire(el);
    document.getElementById("supRetur").onclick=()=>{supSag=null;renderSupport();};
    const gk=document.getElementById("supGenaaben");if(gk)gk.onclick=async()=>{await apiKald({a:"sluk",u:window.BPA_USER,sid:sid,til:0}).catch(()=>{});supVisSag(sid);};
    const lk=document.getElementById("supLuk");if(lk)lk.onclick=async()=>{if(confirm("Luk sagen? Du kan altid genåbne den.")){await apiKald({a:"sluk",u:window.BPA_USER,sid:sid,til:1}).catch(()=>{});supVisSag(sid);}};
    const send=document.getElementById("supSvarSend"),inp=document.getElementById("supSvar"),vk=document.getElementById("supSvarVedh");
    const up=vk?supUploader(document.getElementById("supSvarFiler"),{u:window.BPA_USER}):null;
    if(vk)vk.onclick=()=>up.vaelg();
    async function sSend(){const v=(inp.value||"").trim();if(!v&&(!up||!up.filer.length))return;inp.value="";
        try{await apiKald({a:"ssend",u:window.BPA_USER,sid:sid,txt:v,filer:up?up.filer:[]});supVisSag(sid);}catch(e){}}
    if(send)send.onclick=sSend;if(inp)inp.addEventListener("keydown",e=>{if(e.key==="Enter")sSend();});
}

/* ===================== 📌 Mine noter — borgerens private post-it-tavle ===================== */
var bnotData=null,bnotRedigId=null,bnotVisArkiv=false,bnotFilterKat=null,bnotKatAaben=false,bnotKatArb=null;
function bnotKatNavn(id){var ks=(bnotData&&bnotData.kategorier)||[];for(var i=0;i<ks.length;i++)if(ks[i].id===id)return ks[i].navn;return "";}
function bnotHent(){return apiKald({a:"bnoter",u:window.BPA_USER}).then(function(j){if(j&&j.ok)bnotData=j;return (j&&j.ok)?j:null;}).catch(function(){return null;});}
function bnotKortHTML(n){
    var hex=NOT_HEX[n.farve]||NOT_HEX.gul, katN=n.kat?bnotKatNavn(n.kat):"";
    return '<div class="bnot-kort" data-id="'+fbaEsc(n.id)+'" style="break-inside:avoid;margin:0 0 12px;background:'+hex+';border-radius:14px;padding:'+(n.pin?'8px 16px 14px':'14px 16px')+';box-shadow:'+(n.pin?'0 0 0 2.5px var(--moerk),':'')+'0 8px 22px rgba(30,55,76,.18);position:relative">'+
        (n.pin?'<div style="font-size:.68rem;font-weight:800;letter-spacing:.04em;text-transform:uppercase;color:var(--moerk);margin:0 0 6px;display:flex;align-items:center;gap:4px">📌 Fastgjort</div>':'')+
        (katN?'<div style="font-size:.72rem;font-weight:800;color:#2a3a48;opacity:.75;margin-bottom:4px">'+fbaEsc(katN)+'</div>':'')+
        '<div style="white-space:pre-wrap;font-size:.94rem;color:#22303c;word-break:break-word">'+fbaEsc(n.txt||"")+'</div>'+
        '<div style="display:flex;gap:5px;margin-top:10px;flex-wrap:wrap">'+
        (bnotVisArkiv
            ?'<button class="bnot-k bn-gendan" data-id="'+fbaEsc(n.id)+'">↩ Gendan</button><button class="bnot-k bn-slet" data-id="'+fbaEsc(n.id)+'" style="color:var(--roed)">🗑 Slet</button>'
            :'<button class="bnot-k bn-red" data-id="'+fbaEsc(n.id)+'">✏️ Rediger</button><button class="bnot-k bn-pin" data-id="'+fbaEsc(n.id)+'">'+(n.pin?"📌 Frigør":"📌 Fastgør")+'</button><button class="bnot-k bn-ark" data-id="'+fbaEsc(n.id)+'">🗄 Arkivér</button>')+
        '</div></div>';
}
function bnotRedigHTML(n){
    var hex=NOT_HEX[n.farve]||NOT_HEX.gul;
    return '<div class="bnot-redig" data-id="'+(n.id||"")+'" data-farve="'+(n.farve||"gul")+'" style="break-inside:avoid;margin:0 0 12px;background:'+hex+';border-radius:14px;padding:14px 16px;box-shadow:0 10px 26px rgba(30,55,76,.22)">'+
        '<textarea class="bnr-txt" rows="4" maxlength="2000" placeholder="Skriv din seddel …" style="width:100%;box-sizing:border-box;border:0;background:rgba(255,255,255,.55);border-radius:9px;padding:9px 11px;font-family:var(--font);font-size:.92rem;resize:vertical">'+fbaEsc(n.txt||"")+'</textarea>'+
        '<div style="display:flex;gap:6px;margin:9px 0;align-items:center">'+
        Object.keys(NOT_HEX).map(function(f){return '<button class="bnr-farve" data-f="'+f+'" title="'+f+'" style="width:24px;height:24px;border-radius:50%;cursor:pointer;background:'+NOT_HEX[f]+';border:2px solid '+(f===(n.farve||"gul")?"#1e374c":"rgba(30,55,76,.15)")+'"></button>';}).join("")+
        '<span style="flex:1"></span>'+
        '<label style="font-size:.75rem;color:#2a3a48;display:flex;align-items:center;gap:4px"><input type="checkbox" class="bnr-pin"'+(n.pin?" checked":"")+'> 📌 fastgør</label></div>'+
        '<select class="bnr-kat" style="width:100%;box-sizing:border-box;border:0;background:rgba(255,255,255,.55);border-radius:9px;padding:7px 9px;font-family:var(--font);font-size:.8rem;margin:0 0 9px">'+
        '<option value="">🏷 Uden kategori</option>'+
        ((bnotData&&bnotData.kategorier)||[]).map(function(k){return '<option value="'+fbaEsc(k.id)+'"'+(n.kat===k.id?" selected":"")+'>'+fbaEsc(k.navn)+'</option>';}).join("")+
        '</select>'+
        '<div style="display:flex;gap:8px"><button class="bnot-k bnr-gem" data-id="'+(n.id||"")+'" style="font-weight:800">💾 Gem</button><button class="bnot-k bnr-fortryd">Fortryd</button></div>'+
        '</div>';
}
function bnotKatTegn(){
    var box=document.getElementById("bnotKatBoks");if(!box)return;
    if(!bnotKatAaben){box.innerHTML="";return;}
    if(!bnotKatArb)bnotKatArb=((bnotData&&bnotData.kategorier)||[]).map(function(k){return {id:k.id,navn:k.navn};});
    box.innerHTML='<div class="fba-kort" style="box-shadow:none;border:1px solid rgba(30,55,76,.14);margin:0 0 12px"><b style="font-size:.9rem">🏷 Dine kategorier</b>'+
        '<p style="color:var(--graa);font-size:.78rem;margin:3px 0 8px">Omdøb, tilføj eller slet. Sedler i en slettet kategori beholdes — de vises bare uden kategori.</p>'+
        '<div id="bnotKatListe"></div>'+
        '<div style="margin-top:6px"><button id="bnotKatTilf" class="bnot-k" style="background:#eef3f7">➕ Ny kategori</button> '+
        '<button id="bnotKatGem" class="bnot-k" style="background:var(--groen);color:#0d2a12;font-weight:800">💾 Gem kategorier</button> <span id="bnotKatStatus" style="color:var(--graa);font-size:.8rem"></span></div></div>';
    function katListe(){
        var el=document.getElementById("bnotKatListe");
        el.innerHTML=bnotKatArb.map(function(k,i){return '<div style="display:flex;gap:6px;align-items:center;margin:5px 0"><input class="bnk-navn" data-i="'+i+'" value="'+fbaEsc(k.navn)+'" maxlength="30" style="flex:1;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 9px;font-family:var(--font);font-size:.85rem"><button class="bnot-k bnk-slet" data-i="'+i+'" style="background:#f4f8fb;color:var(--roed)">🗑</button></div>';}).join("");
        el.querySelectorAll(".bnk-navn").forEach(function(x){x.oninput=function(){bnotKatArb[+x.getAttribute("data-i")].navn=x.value;};});
        el.querySelectorAll(".bnk-slet").forEach(function(b){b.onclick=function(){bnotKatArb.splice(+b.getAttribute("data-i"),1);katListe();};});
    }
    katListe();
    document.getElementById("bnotKatTilf").onclick=function(){bnotKatArb.push({id:"",navn:""});katListe();};
    document.getElementById("bnotKatGem").onclick=async function(){
        var st=document.getElementById("bnotKatStatus");this.disabled=true;
        var rene=bnotKatArb.filter(function(k){return (k.navn||"").trim()!=="";});
        try{var r=await apiKald({a:"bnotkatgem",u:window.BPA_USER,kategorier:rene});
            if(r&&r.ok){bnotData.kategorier=r.kategorier;bnotKatArb=null;bnotKatAaben=false;st.textContent="✔ Gemt";renderBorgerNoter();}
            else{this.disabled=false;st.textContent=(r&&r.fejl)||"Kunne ikke gemme";}}
        catch(e){this.disabled=false;st.textContent="Kunne ikke gemme";}
    };
}
async function renderBorgerNoter(){
    var el=document.getElementById("bnotWrap");if(!el)return;
    if(!window.BPA_USER){el.innerHTML='<p style="color:var(--graa)">Log ind på forsiden for at bruge din tavle.</p>';return;}
    if(!bnotData)el.innerHTML='<p style="color:var(--graa)">⏳ Henter din tavle …</p>';
    var d=await bnotHent();
    if(location.hash.replace(/^#\/?/,"").split("?")[0]!=="noter")return;
    if(!d){el.innerHTML='<p style="color:var(--roed)">Kunne ikke hente tavlen — prøv igen.</p>';return;}
    var CHIP=function(a){return 'border:1px solid '+(a?'var(--moerk)':'rgba(30,55,76,.16)')+';background:'+(a?'var(--moerk)':'#f4f8fb')+';color:'+(a?'#fff':'var(--moerk)')+';border-radius:999px;cursor:pointer;font-family:var(--font);font-size:.76rem;font-weight:800;padding:5px 12px';};
    var kats=d.kategorier||[];
    var h='<div style="margin:0 0 12px;display:flex;gap:8px;flex-wrap:wrap;align-items:center">'+
        '<button id="bnotNy" class="bnot-k" style="background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff">➕ Ny seddel</button>'+
        '<button id="bnotArkivBtn" class="bnot-k" style="background:#eef3f7">'+(bnotVisArkiv?"‹ Tilbage til tavlen":"🗄 Arkiv ("+(d.arkiv||[]).length+")")+'</button>'+
        '<button id="bnotKatBtn" class="bnot-k" style="background:#eef3f7">🏷 Kategorier</button></div>'+
        '<div id="bnotKatBoks"></div><div id="bnotNyBoks"></div>';
    if(!bnotVisArkiv&&kats.length){
        h+='<div style="display:flex;gap:6px;flex-wrap:wrap;margin:0 0 12px">'+
            '<button class="bnot-chip" data-k="__alle" style="'+CHIP(bnotFilterKat===null)+'">Alle</button>'+
            kats.map(function(k){return '<button class="bnot-chip" data-k="'+fbaEsc(k.id)+'" style="'+CHIP(bnotFilterKat===k.id)+'">'+fbaEsc(k.navn)+'</button>';}).join("")+
            '<button class="bnot-chip" data-k="" style="'+CHIP(bnotFilterKat==="")+'">🏷 Uden kategori</button></div>';
    }
    var liste=bnotVisArkiv?(d.arkiv||[]):(d.mine||[]);
    if(!bnotVisArkiv&&bnotFilterKat!==null){liste=liste.filter(function(n){return bnotFilterKat===""?!(n.kat&&bnotKatNavn(n.kat)):n.kat===bnotFilterKat;});}
    h+='<div id="bnotGrid" style="column-width:250px;column-gap:12px">'+
        (liste.length?liste.map(function(n){return n.id===bnotRedigId?bnotRedigHTML(n):bnotKortHTML(n);}).join(""):'<p style="color:var(--graa)">'+(bnotVisArkiv?"Arkivet er tomt.":"Ingen sedler endnu — opret din første med ➕ Ny seddel.")+'</p>')+
        '</div>';
    el.innerHTML=h;
    try{window.dispatchEvent(new Event("bnotChanged"));}catch(e){}   /* opdatér fastgjort-striben i toppen */
    document.getElementById("bnotNy").onclick=function(){var b=document.getElementById("bnotNyBoks");b.innerHTML=bnotRedigHTML({});bnotWire(b);b.querySelector(".bnr-txt").focus();};
    document.getElementById("bnotArkivBtn").onclick=function(){bnotVisArkiv=!bnotVisArkiv;bnotRedigId=null;renderBorgerNoter();};
    document.getElementById("bnotKatBtn").onclick=function(){bnotKatAaben=!bnotKatAaben;bnotKatArb=null;bnotKatTegn();};
    bnotKatTegn();
    el.querySelectorAll(".bnot-chip").forEach(function(b){b.onclick=function(){var k=b.getAttribute("data-k");bnotFilterKat=(k==="__alle")?null:k;renderBorgerNoter();};});
    bnotWire(el);
}
function bnotWire(scope){
    scope.querySelectorAll(".bn-red").forEach(function(b){b.onclick=function(){bnotRedigId=b.getAttribute("data-id");renderBorgerNoter();};});
    scope.querySelectorAll(".bn-pin").forEach(function(b){b.onclick=async function(){var id=b.getAttribute("data-id");var n=(bnotData.mine||[]).find(function(x){return x.id===id;});await apiKald({a:"bnotstatus",u:window.BPA_USER,nid:id,pin:n&&n.pin?0:1}).catch(function(){});renderBorgerNoter();};});
    scope.querySelectorAll(".bn-ark").forEach(function(b){b.onclick=async function(){await apiKald({a:"bnotstatus",u:window.BPA_USER,nid:b.getAttribute("data-id"),arkiv:1}).catch(function(){});renderBorgerNoter();};});
    scope.querySelectorAll(".bn-gendan").forEach(function(b){b.onclick=async function(){await apiKald({a:"bnotstatus",u:window.BPA_USER,nid:b.getAttribute("data-id"),arkiv:0}).catch(function(){});renderBorgerNoter();};});
    scope.querySelectorAll(".bn-slet").forEach(function(b){b.onclick=async function(){if(confirm("Slet sedlen permanent?")){await apiKald({a:"bnotslet",u:window.BPA_USER,nid:b.getAttribute("data-id")}).catch(function(){});renderBorgerNoter();}};});
    scope.querySelectorAll(".bnot-redig").forEach(function(red){
        var valgtFarve=red.getAttribute("data-farve")||"gul";
        red.querySelectorAll(".bnr-farve").forEach(function(fb){fb.onclick=function(){valgtFarve=fb.getAttribute("data-f");red.querySelectorAll(".bnr-farve").forEach(function(x){x.style.borderColor=(x===fb)?"#1e374c":"rgba(30,55,76,.15)";});};});
        var fortryd=red.querySelector(".bnr-fortryd");if(fortryd)fortryd.onclick=function(){bnotRedigId=null;document.getElementById("bnotNyBoks").innerHTML="";renderBorgerNoter();};
        var gem=red.querySelector(".bnr-gem");if(gem)gem.onclick=async function(){
            var txt=red.querySelector(".bnr-txt").value.trim();if(!txt){red.querySelector(".bnr-txt").focus();return;}
            var farve=valgtFarve,kat=red.querySelector(".bnr-kat").value,pin=red.querySelector(".bnr-pin").checked?1:0,id=gem.getAttribute("data-id");
            gem.disabled=true;
            try{var r=await apiKald({a:"bnotgem",u:window.BPA_USER,nid:id||"",txt:txt,farve:farve,kat:kat,pin:pin});
                if(r&&r.ok){bnotRedigId=null;document.getElementById("bnotNyBoks").innerHTML="";renderBorgerNoter();}
                else{gem.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){gem.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
        };
    });
}
/* ===================== 📅 Min kalender (borger) — bkal-præfiks (undgår kollision m. admin-Teamup-kalenderen) ===================== */
var bkalData=null;
const BKAL_HEX={blaa:"#3cb4e5",groen:"#7bb800",lilla:"#8b6cef",orange:"#f5a623",pink:"#e7679b",graa:"#7d93a6",roed:"#e7004c"};
const BKAL_UGE=["søndag","mandag","tirsdag","onsdag","torsdag","fredag","lørdag"];
const BKAL_MDR=["januar","februar","marts","april","maj","juni","juli","august","september","oktober","november","december"];
function bkalHent(){
    var ctrl=(typeof AbortController!=="undefined")?new AbortController():null;
    var t=setTimeout(function(){try{if(ctrl)ctrl.abort();}catch(e){}},12000);
    return fetch(API_URL,{method:"POST",body:JSON.stringify({a:"bkalhent",u:window.BPA_USER,btok:(function(){try{return localStorage.getItem("bpa_btok")||"";}catch(e){return "";}})()}),signal:ctrl?ctrl.signal:undefined})
        .then(function(r){return r.json();})
        .then(function(j){clearTimeout(t);if(j&&j.ok)bkalData=j;return (j&&j.ok)?j:null;})
        .catch(function(){clearTimeout(t);return null;});
}
function bkalIso(dd){return dd.getFullYear()+"-"+("0"+(dd.getMonth()+1)).slice(-2)+"-"+("0"+dd.getDate()).slice(-2);}
function bkalKl(d){return ("0"+d.getHours()).slice(-2)+":"+("0"+d.getMinutes()).slice(-2);}
function bkalPlus(hhmm,t){var h=(parseInt(hhmm.slice(0,2),10)+t)%24;return ("0"+h).slice(-2)+hhmm.slice(2);}
function bkalNaeste(){var d=new Date();d.setSeconds(0,0);d.setMinutes(0);d.setHours(d.getHours()+1);return d;}
function bkalDagLabel(dato){
    var d=new Date(dato+"T00:00:00"),na=new Date();na.setHours(0,0,0,0);
    var diff=Math.round((d-na)/864e5);
    var base=BKAL_UGE[d.getDay()]+" "+d.getDate()+". "+BKAL_MDR[d.getMonth()];
    if(diff===0)return "I dag · "+base;
    if(diff===1)return "I morgen · "+base;
    return base.charAt(0).toUpperCase()+base.slice(1);
}
async function renderBKal(){
    var el=document.getElementById("bkalWrap");if(!el)return;
    if(!window.BPA_USER||window.BPA_USER==="faelles"){el.innerHTML='<p style="color:var(--graa)">Log ind på forsiden for at bruge din kalender.</p>';return;}
    if(!bkalData)el.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    var d=await bkalHent();
    if(!d){el.innerHTML='<p style="color:var(--graa)">Kunne ikke hente kalenderen lige nu.</p><button id="bkalProev" style="margin-top:10px;background:#3cb4e5;color:#fff;border:0;border-radius:999px;padding:9px 18px;font-family:var(--font);font-weight:700;cursor:pointer">Prøv igen</button>';var pr=document.getElementById("bkalProev");if(pr)pr.onclick=function(){bkalData=null;renderBKal();};return;}
    var evts=(d.events||[]);
    var h='<div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin:0 0 14px">'+
        '<button id="bkalNy" style="background:linear-gradient(120deg,#3cb4e5,#1e9fd6);color:#fff;border:0;border-radius:999px;padding:11px 20px;font-family:var(--font);font-weight:800;font-size:.95rem;cursor:pointer;box-shadow:0 8px 20px rgba(60,180,229,.35)">＋ Ny aftale</button>'+
        '<span style="flex:1"></span>'+
        '<span style="display:inline-flex;align-items:center;gap:5px;font-size:.76rem;color:var(--graa)"><span style="width:9px;height:9px;border-radius:50%;background:'+BKAL_HEX.blaa+'"></span>Mine</span>'+
        (evts.some(function(e){return e.kilde==="aarshjul";})?'<span style="display:inline-flex;align-items:center;gap:5px;font-size:.76rem;color:var(--graa)"><span style="width:9px;height:9px;border-radius:50%;background:'+BKAL_HEX.lilla+'"></span>Årshjul</span>':'')+
        (d.feeds||[]).map(function(f){return '<span style="display:inline-flex;align-items:center;gap:5px;font-size:.76rem;color:var(--graa)"><span style="width:9px;height:9px;border-radius:50%;background:'+(BKAL_HEX[f.farve]||BKAL_HEX.graa)+'"></span>'+fbaEsc(f.navn)+'</span>';}).join("")+
        '<button id="bkalFeeds" style="border:1px solid rgba(30,55,76,.18);background:#fff;border-radius:999px;padding:6px 12px;font-family:var(--font);font-size:.76rem;font-weight:700;color:var(--moerk);cursor:pointer">🔗 Kalendere</button>'+
        '</div>';
    if(!evts.length){
        h+='<div style="text-align:center;padding:44px 20px;color:var(--graa)"><div style="font-size:2.4rem">📅</div><p style="margin:10px 0 0">Ingen aftaler de kommende måneder.<br>Tryk <b>＋ Ny aftale</b> for at oprette din første.</p></div>';
    }else{
        var sidste=null;
        evts.forEach(function(e){
            if(e.dato!==sidste){sidste=e.dato;h+='<div style="font-size:.8rem;font-weight:800;color:var(--moerk);margin:18px 0 8px">'+fbaEsc(bkalDagLabel(e.dato))+'</div>';}
            var farve=BKAL_HEX[e.farve]||BKAL_HEX.blaa;
            var tidKort=e.heldag?'hele<br>dagen':fbaEsc(e.start);
            var meta=(e.heldag?'Hele dagen':(fbaEsc(e.start)+(e.slut?' – '+fbaEsc(e.slut):'')))+(e.kilde!=="egen"&&e.feedNavn?' · '+fbaEsc(e.feedNavn):'')+(e.note?' · '+fbaEsc(e.note):'');
            h+='<div class="bkal-rk'+(e.redigerbar?' bkal-egen':'')+'"'+(e.redigerbar?' data-id="'+fbaEsc(e.id)+'"':'')+' style="display:flex;gap:12px;align-items:flex-start;padding:11px 12px;border-radius:12px;background:#fff;box-shadow:0 3px 12px rgba(30,55,76,.07);margin-bottom:8px'+(e.redigerbar?';cursor:pointer':'')+'">'+
                '<div style="width:48px;font-size:.78rem;color:var(--graa);font-weight:700;text-align:right;flex-shrink:0;line-height:1.25">'+tidKort+'</div>'+
                '<div style="flex:1;border-left:4px solid '+farve+';padding-left:11px;min-width:0">'+
                '<div style="font-size:.98rem;color:var(--moerk);font-weight:600;word-break:break-word">'+fbaEsc(e.titel)+'</div>'+
                '<div style="font-size:.78rem;color:var(--graa);margin-top:2px;word-break:break-word">'+meta+'</div></div>'+
                (e.redigerbar?'<span style="color:var(--graa);font-size:1.1rem;flex-shrink:0">›</span>':'')+
                '</div>';
        });
    }
    el.innerHTML=h;
    document.getElementById("bkalNy").onclick=function(){bkalSheet(null);};
    document.getElementById("bkalFeeds").onclick=function(){bkalFeedSheet();};
    el.querySelectorAll(".bkal-egen").forEach(function(r){r.onclick=function(){
        var id=r.getAttribute("data-id");var ev=(bkalData.events||[]).find(function(x){return x.id===id&&x.redigerbar;});if(ev)bkalSheet(ev);
    };});
}
function bkalSheet(ev){
    if(document.getElementById("bkalOv"))return;
    var nyTid=bkalNaeste();
    var e=ev||{titel:"",dato:bkalIso(new Date()),start:bkalKl(nyTid),slut:bkalKl(new Date(nyTid.getTime()+36e5)),heldag:0,farve:"blaa",note:"",paamind:0};
    var farver=(bkalData&&bkalData.farver)||["blaa","groen","lilla","orange","pink","graa","roed"];
    var ov=document.createElement("div");ov.id="bkalOv";
    ov.style.cssText="position:fixed;inset:0;z-index:9996;background:rgba(30,55,76,.45);display:flex;align-items:flex-end;justify-content:center";
    ov.innerHTML='<div style="width:min(460px,100%);max-height:92vh;overflow-y:auto;background:#fff;border-radius:22px 22px 0 0;padding:20px 20px 26px;font-family:var(--font);box-shadow:0 -20px 60px rgba(0,0,0,.3)">'+
        '<div style="width:40px;height:4px;background:rgba(30,55,76,.2);border-radius:99px;margin:0 auto 16px"></div>'+
        '<h3 style="margin:0 0 16px;font-size:1.15rem;color:var(--moerk)">'+(ev?"Redigér aftale":"Ny aftale")+'</h3>'+
        '<input id="bkalTitel" value="'+fbaEsc(e.titel)+'" maxlength="120" placeholder="Hvad handler det om?" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.18);border-radius:12px;padding:12px 14px;font-family:var(--font);font-size:1rem;margin:0 0 12px">'+
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:11px 2px;border-top:1px solid rgba(30,55,76,.09)"><label style="font-size:.95rem;color:var(--moerk);font-weight:600">📅 Dato</label><input id="bkalDato" type="date" value="'+fbaEsc(e.dato)+'" style="border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:8px 10px;font-family:var(--font);font-size:.95rem"></div>'+
        '<div id="bkalTidRk" style="display:'+(e.heldag?'none':'flex')+';align-items:center;justify-content:space-between;padding:11px 2px;border-top:1px solid rgba(30,55,76,.09)"><label style="font-size:.95rem;color:var(--moerk);font-weight:600">🕐 Tidspunkt</label><span style="white-space:nowrap"><input id="bkalStart" type="time" value="'+fbaEsc(e.start||"09:00")+'" style="border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:8px 8px;font-family:var(--font)"> – <input id="bkalSlut" type="time" value="'+fbaEsc(e.slut||"10:00")+'" style="border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:8px 8px;font-family:var(--font)"></span></div>'+
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:11px 2px;border-top:1px solid rgba(30,55,76,.09)"><label for="bkalHeldag" style="font-size:.95rem;color:var(--moerk);font-weight:600">☀️ Hele dagen</label><input id="bkalHeldag" type="checkbox"'+(e.heldag?" checked":"")+' style="width:22px;height:22px"></div>'+
        '<div style="padding:11px 2px;border-top:1px solid rgba(30,55,76,.09)"><div style="font-size:.95rem;color:var(--moerk);font-weight:600;margin-bottom:8px">🎨 Farve</div><div id="bkalFarver" style="display:flex;gap:8px;flex-wrap:wrap">'+
        farver.map(function(f){return '<button type="button" class="bkal-fv" data-f="'+f+'" style="width:30px;height:30px;border-radius:50%;cursor:pointer;background:'+(BKAL_HEX[f]||BKAL_HEX.graa)+';border:3px solid '+(f===e.farve?"#1e374c":"transparent")+'"></button>';}).join("")+'</div></div>'+
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:11px 2px;border-top:1px solid rgba(30,55,76,.09)"><label style="font-size:.95rem;color:var(--moerk);font-weight:600">🔔 Påmindelse</label><select id="bkalPaamind" style="border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:8px 10px;font-family:var(--font);font-size:.95rem">'+
        [[0,"Ingen"],[10,"10 min før"],[30,"30 min før"],[60,"1 time før"],[120,"2 timer før"],[1440,"1 dag før"]].map(function(o){return '<option value="'+o[0]+'"'+(e.paamind===o[0]?" selected":"")+'>'+o[1]+'</option>';}).join("")+'</select></div>'+
        '<textarea id="bkalNote" maxlength="500" rows="2" placeholder="Note (valgfri) …" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.14);border-radius:12px;padding:10px 12px;font-family:var(--font);font-size:.92rem;margin:12px 0 0;resize:vertical">'+fbaEsc(e.note||"")+'</textarea>'+
        '<div style="display:flex;gap:8px;margin-top:16px;align-items:center">'+
        '<button id="bkalGem" style="flex:1;background:#93d500;color:#1a2e05;border:0;border-radius:12px;padding:13px;font-family:var(--font);font-weight:800;font-size:1rem;cursor:pointer">Gem aftale</button>'+
        '<button id="bkalAnnul" style="background:#fff;border:1px solid rgba(30,55,76,.2);border-radius:12px;padding:13px 18px;font-family:var(--font);font-size:1rem;cursor:pointer;color:var(--graa)">Fortryd</button>'+
        (ev?'<button id="bkalSlet" title="Slet aftale" style="background:#fff;border:1px solid rgba(231,0,76,.4);border-radius:12px;padding:13px 15px;font-family:var(--font);font-size:1rem;cursor:pointer;color:var(--roed)">🗑</button>':'')+
        '</div></div>';
    document.body.appendChild(ov);
    var valgtFarve=e.farve;
    ov.querySelectorAll(".bkal-fv").forEach(function(b){b.onclick=function(){valgtFarve=b.getAttribute("data-f");ov.querySelectorAll(".bkal-fv").forEach(function(x){x.style.border="3px solid "+(x===b?"#1e374c":"transparent");});};});
    var heldag=document.getElementById("bkalHeldag");
    heldag.onchange=function(){document.getElementById("bkalTidRk").style.display=heldag.checked?"none":"flex";};
    var start=document.getElementById("bkalStart");
    if(start)start.onchange=function(){var s=document.getElementById("bkalSlut");if(s&&(!s.value||s.value<=start.value))s.value=bkalPlus(start.value,1);};
    var luk=function(){ov.remove();};
    ov.querySelector("#bkalAnnul").onclick=luk;
    ov.addEventListener("click",function(z){if(z.target===ov)luk();});
    var slet=document.getElementById("bkalSlet");
    if(slet)slet.onclick=async function(){if(!confirm("Slet aftalen?"))return;this.disabled=true;try{await apiKald({a:"bkalslet",u:window.BPA_USER,nid:e.id});luk();renderBKal();bkalFliseOpdater();}catch(x){this.disabled=false;}};
    document.getElementById("bkalGem").onclick=async function(){
        var payload={a:"bkalgem",u:window.BPA_USER,nid:(ev?e.id:""),titel:document.getElementById("bkalTitel").value.trim(),dato:document.getElementById("bkalDato").value,
            heldag:heldag.checked?1:0,start:document.getElementById("bkalStart").value,slut:document.getElementById("bkalSlut").value,
            farve:valgtFarve,paamind:document.getElementById("bkalPaamind").value,note:document.getElementById("bkalNote").value.trim()};
        if(!payload.dato){alert("Vælg en dato.");return;}
        this.disabled=true;
        try{var r=await apiKald(payload);if(r&&r.ok){luk();renderBKal();bkalFliseOpdater();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(x){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
}
function bkalFeedSheet(){
    if(document.getElementById("bkalFOv"))return;
    var feeds=(bkalData&&bkalData.feeds)||[];
    var farver=(bkalData&&bkalData.farver)||["graa","blaa","groen","lilla","orange","pink","roed"];
    var ov=document.createElement("div");ov.id="bkalFOv";
    ov.style.cssText="position:fixed;inset:0;z-index:9997;background:rgba(30,55,76,.45);display:flex;align-items:flex-end;justify-content:center";
    ov.innerHTML='<div style="width:min(460px,100%);max-height:92vh;overflow-y:auto;background:#fff;border-radius:22px 22px 0 0;padding:20px 20px 26px;font-family:var(--font)">'+
        '<div style="width:40px;height:4px;background:rgba(30,55,76,.2);border-radius:99px;margin:0 auto 16px"></div>'+
        '<h3 style="margin:0 0 6px;font-size:1.15rem;color:var(--moerk)">🔗 Eksterne kalendere</h3>'+
        '<p style="color:var(--graa);font-size:.82rem;margin:0 0 14px">Indsæt et iCal-link (.ics) fra fx Google, Outlook eller Apple, så vises deres aftaler her — skrivebeskyttet. I Google Kalender: Indstillinger → din kalender → «Hemmelig adresse i iCal-format».</p>'+
        (feeds.length?feeds.map(function(f){return '<div style="display:flex;align-items:center;gap:10px;padding:10px 0;border-top:1px solid rgba(30,55,76,.08)"><span style="width:12px;height:12px;border-radius:50%;background:'+(BKAL_HEX[f.farve]||BKAL_HEX.graa)+';flex-shrink:0"></span><span style="flex:1;font-size:.92rem;color:var(--moerk);word-break:break-all">'+fbaEsc(f.navn)+'</span><button class="bkalF-slet" data-id="'+fbaEsc(f.id)+'" style="border:0;background:none;cursor:pointer;color:var(--roed);font-size:1rem">🗑</button></div>';}).join(""):'<p style="color:var(--graa);font-size:.85rem;margin:0">Ingen tilføjet endnu.</p>')+
        '<div style="margin-top:14px;padding-top:14px;border-top:1px solid rgba(30,55,76,.12)">'+
        '<input id="bkalFNavn" maxlength="60" placeholder="Navn (fx «Google»)" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:10px 12px;font-family:var(--font);margin:0 0 8px">'+
        '<input id="bkalFUrl" maxlength="500" placeholder="https://…/basic.ics" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:10px 12px;font-family:var(--font);margin:0 0 8px">'+
        '<div style="display:flex;gap:7px;flex-wrap:wrap;margin:0 0 12px" id="bkalFFarver">'+farver.map(function(f,i){return '<button type="button" class="bkalF-fv" data-f="'+f+'" style="width:26px;height:26px;border-radius:50%;cursor:pointer;background:'+(BKAL_HEX[f]||BKAL_HEX.graa)+';border:3px solid '+(i===0?"#1e374c":"transparent")+'"></button>';}).join("")+'</div>'+
        '<div style="display:flex;gap:8px"><button id="bkalFTilf" style="flex:1;background:#3cb4e5;color:#fff;border:0;border-radius:12px;padding:12px;font-family:var(--font);font-weight:800;cursor:pointer">Tilføj kalender</button>'+
        '<button id="bkalFLuk" style="background:#fff;border:1px solid rgba(30,55,76,.2);border-radius:12px;padding:12px 18px;font-family:var(--font);cursor:pointer;color:var(--graa)">Luk</button></div>'+
        '</div></div>';
    document.body.appendChild(ov);
    var farve=farver[0];
    ov.querySelectorAll(".bkalF-fv").forEach(function(b){b.onclick=function(){farve=b.getAttribute("data-f");ov.querySelectorAll(".bkalF-fv").forEach(function(x){x.style.border="3px solid "+(x===b?"#1e374c":"transparent");});};});
    var luk=function(){ov.remove();};
    ov.querySelector("#bkalFLuk").onclick=luk;
    ov.addEventListener("click",function(z){if(z.target===ov)luk();});
    ov.querySelectorAll(".bkalF-slet").forEach(function(b){b.onclick=async function(){if(!confirm("Fjern denne kalender?"))return;await apiKald({a:"bkalfeedslet",u:window.BPA_USER,fid:b.getAttribute("data-id")}).catch(function(){});luk();bkalData=null;renderBKal();};});
    ov.querySelector("#bkalFTilf").onclick=async function(){
        var url=document.getElementById("bkalFUrl").value.trim();
        if(!url){alert("Indsæt et iCal-link.");return;}
        this.disabled=true;
        try{var r=await apiKald({a:"bkalfeedgem",u:window.BPA_USER,navn:document.getElementById("bkalFNavn").value.trim(),url:url,farve:farve});
            if(r&&r.ok){luk();bkalData=null;renderBKal();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke tilføje");}}
        catch(x){this.disabled=false;alert("Kunne ikke tilføje — tjek linket.");}
    };
}
function bkalFliseOpdater(){
    var el=document.getElementById("bkalFliseNaeste");if(!el||!window.BPA_USER||window.BPA_USER==="faelles")return;
    apiKald({a:"bkalhent",u:window.BPA_USER}).then(function(j){
        if(!j||!j.ok)return;bkalData=j;
        var nu=Date.now()/1000;
        var naeste=(j.events||[]).filter(function(e){return e.slutTs>=nu;})[0];
        if(!naeste)return;
        var d=new Date(naeste.startTs*1000);
        var diff=Math.round((new Date(naeste.dato+"T00:00:00")-new Date(new Date().toDateString()))/864e5);
        var dagTxt=diff===0?"i dag":(diff===1?"i morgen":(d.getDate()+". "+BKAL_MDR[d.getMonth()]));
        el.textContent="Næste: "+naeste.titel+" · "+dagTxt+(naeste.heldag?"":" kl. "+bkalKl(d));
    }).catch(function(){});
}
/* ===================== Router (hash-visninger) ===================== */
const VISNINGER={"":"v-hjem","formularer":"v-formularer","kontakt":"v-kontakt","faq":"v-faq","nyheder":"v-nyheder","hjemmeside":"v-hjemmeside","mithf":"v-mithf","formular":"v-formvis","feedback-admin":"v-fbadmin","nyheds-admin":"v-nyadmin","intern":"v-intern","aarshjul":"v-aarshjul","faellesskab":"v-faelles","support":"v-support","noter":"v-noter","kalender":"v-kalender"};
function router(){
    const raw=location.hash.replace(/^#\/?/,"");
    const qi=raw.indexOf("?");
    const del=qi>=0?raw.slice(0,qi):raw;
    const query=qi>=0?raw.slice(qi+1):"";
    const supDeep=(()=>{try{return new URLSearchParams(query).get("sup")||"";}catch(e){return"";}})();
    const id=VISNINGER[del]||"v-hjem";
    /* HÅRD GATE: adm-visninger renderes ALDRIG uden admin-session — heller ikke skelettet
     (område-piller m.m.). En borger m. #/feedback-admin i URL'en (historik/autoudfyld)
     sendes til forsiden. Serverens data-afvisning består som ekstra lag nedenunder. */
    if(id==="v-fbadmin"||id==="v-nyadmin"||id==="v-intern"){
        let s=null;try{s=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
        if(!s||!s.token){location.replace("#/");return;}
    }
    document.querySelectorAll(".visning").forEach(v=>v.classList.toggle("vis",v.id===id));
    document.querySelectorAll("[data-nav]").forEach(a=>{
        a.classList.toggle("aktiv",(VISNINGER[a.getAttribute("data-nav")==="hjem"?"":a.getAttribute("data-nav")]||"v-hjem")===id&&(id!=="v-hjem"||a.getAttribute("data-nav")==="hjem"));
    });
    window.scrollTo({top:0,behavior:"instant" in window?"instant":"auto"});
    if(id==="v-hjemmeside")startSkal("skalFrame","skalNote","https://handicapformidlingen.dk/");
    if(id==="v-mithf")startSkal("mithfFrame","mithfNote","https://handicapformidlingen.temponizer.dk/");
    if(id==="v-formvis"&&!document.getElementById("formFrame").getAttribute("src")){location.replace("#/formularer");return;} /* direkte link uden valgt formular */
    if(id==="v-fbadmin"){if(supDeep){supAdmSag=supDeep;try{sessionStorage.setItem("omr_fokus","support");}catch(e){}}hentFbAdmin();}
    if(id==="v-nyadmin")renderNyAdmin();
    if(id==="v-intern"){renderIntern();
        if(window.HF_POPOUT){var _n=0,_t=setInterval(function(){var w=document.getElementById("inWrap");
            if(!w||location.hash.indexOf("intern")<0||(++_n>4)){clearInterval(_t);return;}
            if(/⏳ Henter/.test(w.textContent)){renderIntern();}else{clearInterval(_t);}},800);}}  /* popout: gen-forsøg hvis kanaler ikke nåede at loade v. frisk load */
    if(id==="v-aarshjul")renderAarshjulSide();
    if(id==="v-faelles")renderFaelles();
    if(id==="v-support"){if(supDeep)supSag=supDeep;renderSupport();}
    if(id==="v-noter")renderBorgerNoter();
    if(id==="v-kalender")renderBKal();
    try{poFabVis(id);}catch(e){}   /* ⧉ flydende pop-ud-knap: vis kun på admin-sider */
    if(id==="v-nyheder")hentNyheder();
    if(id==="v-kontakt")hentMinKons();
    if(id!=="v-hjem"){const uv=document.getElementById("uniVid");if(uv&&!uv.paused)uv.pause();} /* pausér videoen når man forlader forsiden */
    sætReveal();
}
window.addEventListener("hashchange",router);
/* kør FØRSTE router-kald efter hele scriptet er evalueret, så alle top-level let/const
   (fx inData, kanBeskeder m.fl.) er initialiseret — ellers TDZ-krak v. koldt load direkte på fx #/intern */
setTimeout(router,0);
/* mitHF-BPA-flisen: adgangen er endnu ikke klar for borgerne → vis en pæn «på vej»-popup i stedet for at åbne Temponizer */
function borgerMithfKommer(){
    if (document.getElementById("mithfKomOv")) return;

    var ov = document.createElement("div");
    ov.id="mithfKomOv";
    ov.style.cssText="position:fixed;inset:0;z-index:9996;background:rgba(30,55,76,.5);display:flex;align-items:center;justify-content:center;padding:20px";
    ov.innerHTML='<div style="width:min(440px,100%);max-height:92vh;overflow-y:auto;background:#fff;border-radius:24px;box-shadow:0 30px 80px rgba(0,0,0,.4);font-family:var(--font)">'+
        '<div style="background:linear-gradient(135deg,#3cb4e5,#1e374c);color:#fff;padding:26px 26px 22px;text-align:center;border-radius:24px 24px 0 0">'+
        '<div style="font-size:2.7rem;line-height:1">🗓️</div>'+
        '<h2 style="margin:8px 0 0;font-size:1.35rem">mitHF-BPA er lige på trapperne</h2></div>'+
        '<div style="padding:22px 26px 6px;color:#2a3a48;font-size:.98rem;line-height:1.6">'+
        '<p style="margin:0 0 12px">Vi er ved at lægge sidste hånd på din adgang, så du kan planlægge dine vagter direkte herfra — sikkert og med ét enkelt klik.</p>'+
        '<p style="margin:0 0 12px">Så snart alt er klar, kan du logge ind her på kassen, og vi sender selvfølgelig en <b>nyhed</b> ud her i universet, så du ikke går glip af det.</p>'+
        '<div style="background:#eef7ec;border-radius:14px;padding:12px 14px;margin:14px 0 4px;font-size:.9rem;color:#3b6d11"><b>💡 Vil du være helt klar?</b><br>Øv dig risikofrit i <b>Læringshubben</b> imens — så kender du systemet fra dag ét.</div>'+
        '</div>'+
        '<div style="display:flex;gap:10px;padding:16px 26px 22px;flex-wrap:wrap">'+
        '<a href="laeringshub/index.html" style="flex:1;min-width:150px;text-align:center;text-decoration:none;background:#93d500;color:#1a2e05;border-radius:12px;padding:13px;font-weight:800">🎓 Åbn Læringshubben</a>'+
        '<button id="mithfKomLuk" style="background:#fff;border:1px solid rgba(30,55,76,.2);border-radius:12px;padding:13px 20px;font-family:var(--font);font-size:1rem;cursor:pointer;color:var(--graa)">Luk</button>'+
        '</div></div>';
    document.body.appendChild(ov);
    ov.querySelector("#mithfKomLuk").onclick = function() {
        ov.remove();
    };
    ov.addEventListener("click",function(e){if(e.target===ov)ov.remove();});
}
/* SSO-login til mitHF (Temponizer): POSTer borgerens btok til mithflogin.php, som server-side
   slår borgerens brugere-id op og logger ind. Hvorvidt DENNE borger må, afgøres server-side
   (ssomig → window.BPA_SSO_AKTIV, sat ved login) og styres fra adm-området «mitHF-adgang».
   Borgere uden adgang får «på vej»-popup'en. */
function aabnMithfSSO(){
    var btok="";
    try {
        btok = localStorage.getItem("bpa_btok")||"";
    } catch (e) {}

    if(!btok){borgerMithfKommer();return;}   /* intet login-token → vis popup i stedet */

    /* åbn et rent app-vindue (uden fanelinje/menuer) og POST ind i det.
     Browsere kan ikke skjule domænet helt, men vinduet er så diskret som muligt. */
    var bw=Math.max(360,Math.min(1280,(screen.availWidth||1200)-80));
    var bh=Math.max(480,Math.min(920,(screen.availHeight||820)-80));
    var bx=Math.round(((screen.availWidth||1200)-bw)/2),by=Math.round(((screen.availHeight||820)-bh)/2);
    var feat="width="+bw+",height="+bh+",left="+bx+",top="+by+",resizable=yes,scrollbars=yes,menubar=no,toolbar=no,location=no,status=no";

    var vin=null;
    try{
        vin=window.open("about:blank","mithfBpaVindue",feat);
    }catch(e){}

    var f = document.createElement("form");
    f.method = "POST";
    f.action = "mithflogin.php";
    f.target = vin ? "mithfBpaVindue" : "_blank";   /* popup hvis tilladt, ellers ny fane */

    var i = document.createElement("input");
    i.type="hidden";
    i.name="btok";
    i.value=btok;
    f.appendChild(i);
    document.body.appendChild(f);
    f.submit();
    setTimeout(function(){try{f.remove();}catch(e){}},3000);
}
/* ADMIN: «Log på borgerweb» — log ind SOM borgeren i mitHF, i et minimalistisk popup IDENTISK
   med borgerens eget. Server-handlingen ssoadmin verificerer admin + rettigheden 'borgerweb'
   og signerer SSO-kaldet; borger-gaten springes over, så personale ALTID kan komme ind — også
   hvis borgerens egen mitHF-adgang ikke er aktiveret. Borgerens eget flow røres ikke. */
function aabnBorgerwebAdm(u,navn){
    if(!u)return;
    /* åbn popup'en SYNKRONT på klikket (ellers blokerer browseren den) — samme vinduesmål som borgerens.
     GENBRUGER samme vinduesnavn, så vi ikke ophober vinduer. */
    var bw=Math.max(360,Math.min(1500,(screen.availWidth||1280)-40));   /* bredere: så mitHF viser sin desktop-topmenu, ikke mobil-menuen */
    var bh=Math.max(480,Math.min(1000,(screen.availHeight||860)-40));
    var bx=Math.round(((screen.availWidth||1200)-bw)/2),by=Math.round(((screen.availHeight||820)-bh)/2);
    var feat="width="+bw+",height="+bh+",left="+bx+",top="+by+",resizable=yes,scrollbars=yes,menubar=no,toolbar=no,location=no,status=no";

    var vin = window.open("about:blank","mithfBorgerwebVindue",feat);
    try {
        vin.document.write('<!doctype html><meta charset="utf-8"><title>Skifter borger…</title><body style="font-family:sans-serif;text-align:center;margin-top:70px;color:#1e374c"><p>Logger ind som '+(navn?String(navn).replace(/[<>&]/g," "):"borgeren")+'…</p>');
    } catch (e) {}

    apiKald({a:"ssoadmin",token:fbaSess().token,u:u}).then(function(r){
        /* selve SSO-login'et (POST til Temponizer i popup-vinduet) */
        function sendSSO(){
            var loginForm= document.createElement("form");
            loginForm.method="POST";
            loginForm.action=r.sso_url;
            loginForm.target = vin ? "mithfBorgerwebVindue" : "_blank";

            [["brugere_id",r.brugere_id],["t",r.t],["token",r.token]]
                .forEach(function(kv){
                var i = document.createElement("input");
                i.type="hidden";
                i.name=kv[0];
                i.value=kv[1];
                loginForm.appendChild(i);
            });
            document.body.appendChild(loginForm);
            loginForm.submit();
            setTimeout(function(){try{loginForm.remove();}catch(e){}},3000);
        }
        /* VIGTIGT: log FØRST ud af Temponizer i vinduet — ellers bliver en tidligere borgers session
       hængende (browserens Temponizer-cookie er delt), og så viser vinduet den FORRIGE borger.
       Vi logger ud → når logud-siden er loadet, logger vi den NYE borger ind. Fallback-timer
       sikrer login selv hvis onload ikke fyrer (cross-origin). */
        var logoutUrl="";
        try{logoutUrl=new URL(r.sso_url).origin+"/index.php?page=logout";}catch(e){logoutUrl="";}
        if(vin&&logoutUrl){
            var gjort=false,naeste=function(){if(gjort)return;gjort=true;sendSSO();};
            try{vin.onload=naeste;}catch(e){}
            try{vin.location.href=logoutUrl;}catch(e){naeste();return;}
            setTimeout(naeste,1600);
        }else{ sendSSO(); }
    }).catch(function(){ if(vin){try{vin.close();}catch(e){}} alert("Kunne ikke nå serveren."); });
}
function mithfKlik(){
    if (window.BPA_SSO_AKTIV) {
        aabnMithfSSO();
        return;
    }   /* serveren har givet DENNE borger adgang */
    borgerMithfKommer();
}
(function(){var _mf=document.getElementById("mithfFlise");if(_mf)_mf.addEventListener("click",function(e){e.preventDefault();mithfKlik();});
    var _mt=document.getElementById("mithfTopLink");if(_mt)_mt.addEventListener("click",function(e){e.preventDefault();mithfKlik();});})();
/* Forsidens kasser: anvend admin-styret rækkefølge + vis/skjul */
function forsideAnvend(){
    var wrap=document.getElementById("forsideFliser");if(!wrap)return;
    apiKald({a:"forsidehent"}).then(function(j){
        if(!(j&&j.ok))return;
        var skjul=j.skjul||[],orden=j.orden||[],navn=j.navn||{},badge=j.badge||{},tekst=j.tekst||{};
        var fliser=Array.prototype.slice.call(wrap.querySelectorAll(".flise"));
        fliser.forEach(function(el){
            var id=el.getAttribute("data-flise");
            el.style.display=(skjul.indexOf(id)>=0)?"none":"";
            if(navn[id]){var h=el.querySelector("h3");if(h)h.textContent=navn[id];}
            if(tekst[id]){var p=el.querySelector("p");if(p)p.textContent=tekst[id];}
            if(Object.prototype.hasOwnProperty.call(badge,id)){
                var ny=el.querySelector(".ny");
                if(badge[id]===""){if(ny)ny.remove();}
                else{if(!ny){ny=document.createElement("span");ny.className="ny";el.appendChild(ny);}ny.textContent=badge[id];}
            }
        });
        if(orden.length){
            var byId={};fliser.forEach(function(el){byId[el.getAttribute("data-flise")]=el;});
            orden.forEach(function(id){if(byId[id]){wrap.appendChild(byId[id]);delete byId[id];}});
            fliser.forEach(function(el){if(byId[el.getAttribute("data-flise")])wrap.appendChild(el);});
        }
    }).catch(function(){});
}
forsideAnvend();
window.addEventListener("bnotBoardRefresh",function(){if(location.hash.replace(/^#\/?/,"").split("?")[0]==="noter"&&typeof renderBorgerNoter==="function")renderBorgerNoter();});
/* HF-adm topbar: rød+antal på Beskeder / Feedback / Live-chat */
function hfAdmTael(){
    var s=null;try{s=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
    if(!s||!s.token)return;
    apiKald({a:"admtael",token:s.token}).then(function(j){
        if(!(j&&j.ok))return;
        window.hfTael={beskeder:j.beskeder||0,feedback:j.feedback||0,chat:j.chat||0};
        function saet(id,n){var el=document.querySelector("#"+id+" .hf-adm-tal");if(el){if(n>0){el.textContent=n;el.style.display="";}else{el.style.display="none";}}}
        saet("hfAdmBesk",j.beskeder||0);saet("hfAdmFb",j.feedback||0);saet("hfAdmChat",j.chat||0);saet("hfAdmIsup",j.isup||0);
        window.hfIsupBadge=j.isup||0;try{if(typeof window.hfInternFaneRefresh==="function")window.hfInternFaneRefresh();}catch(e){}
        /* hold overbliks-kortene + Dagens overblik-linjen i sync med de friske tal — uden reload */
        try{if(typeof admOmr!=="undefined"&&admOmr==="dash"&&typeof renderGenveje==="function")renderGenveje();}catch(e){}
        try{if(document.getElementById("lcaDigest")&&typeof renderDigest==="function")renderDigest();}catch(e){}
    }).catch(function(){});
}

/* ===================== Skal-visninger (indlæses første gang de åbnes) ===================== */
function startSkal(frameId,noteId,url){
    const fr=document.getElementById(frameId),note=document.getElementById(noteId);
    if(!fr||fr.dataset.klar)return;
    fr.dataset.klar="1";
    fr.addEventListener("load",()=>{setTimeout(()=>{if(note)note.style.display="none";},1200);});
    fr.src=url;
}

/* ===================== Formularer direkte i siden (Wrike/Drive tillader indlejring — målt 04.07.2026) ===================== */
function åbnFormular(src,orig,titel){
    const fr=document.getElementById("formFrame"),note=document.getElementById("formNote");
    document.getElementById("formTitel").textContent="📝 "+titel;
    document.getElementById("formNyFane").href=orig;
    if(fr.getAttribute("src")!==src){note.style.display="";fr.src=src;} /* samme formular igen = behold udfyldte felter */
    location.hash="#/formular";
}
(function(){
    const fr=document.getElementById("formFrame"),note=document.getElementById("formNote");
    if(!fr)return;
    fr.addEventListener("load",()=>{setTimeout(()=>{note.style.display="none";},900);});
    document.querySelectorAll("#v-formularer .f-kort").forEach(a=>{
        const h=a.getAttribute("href");
        if(!h)return;                                     /* «kommer snart»-kort er <div> uden href → spring over */
        let src=null;
        if(/wrike\.com\/(form\/|frontend\/requestforms)/.test(h))src=h;                       /* Wrike-formularer kan indlejres */
        else{const m=h.match(/drive\.google\.com\/file\/d\/([^/]+)/);if(m)src="https://drive.google.com/file/d/"+m[1]+"/preview";} /* Drive-PDF: preview-visning */
        if(!src)return;                                   /* øvrige (fx ledsagekort på hjemmesiden) åbner i ny fane som før */
        a.addEventListener("click",e=>{
            if(e.ctrlKey||e.metaKey||e.shiftKey)return;     /* lad "åbn i ny fane"-genveje virke */
            e.preventDefault();
            åbnFormular(src,h,(a.querySelector("b")||{textContent:"Formular"}).textContent);
        });
    });
})();

/* ===================== Personlig konsulent (Kontakt-siden) ===================== */
let minKonsHentet=false;
async function hentMinKons(){
    if(minKonsHentet)return;
    const box=document.getElementById("minKons");
    let u="";try{u=(localStorage.getItem("bpa_user")||"").trim().toLowerCase();}catch(e){}
    if(!box||!u)return;
    minKonsHentet=true;
    try{
        const j=await apiKald({a:"minkons",u:u});
        if(!(j&&j.ok&&j.kons&&j.kons.navn))return;
        const k=j.kons;
        const pentTlf=(k.tlf||"").replace(/^(\d{2})(\d{3})(\d{3})$/,"$1 $2 $3");
        box.innerHTML='<div class="kons-kort">'+
            (k.foto?'<img class="kons-foto" src="'+fbaEsc(k.foto)+'" alt="'+fbaEsc(k.navn)+'" onerror="this.remove()">':'')+
            '<div class="kons-info"><div class="kl">🤝 Din faste konsulent</div>'+
            '<h3>'+fbaEsc(k.navn)+'</h3><p>'+fbaEsc(k.titel||"Konsulent")+'</p></div>'+
            '<div class="kons-knapper">'+
            (k.tlf?'<a class="knap" href="tel:'+fbaEsc(k.tlf)+'">📞 '+fbaEsc(pentTlf)+'</a>':'')+
            '<button type="button" class="knap groen" id="konsSkrivBesked">✉️ Skriv til '+fbaEsc((k.navn||"").split(" ")[0])+'</button>'+
            '</div></div>';
        box.hidden=false;
        const sk=document.getElementById("konsSkrivBesked");
        if(sk)sk.onclick=function(){window.__supNyStart=1;location.hash="#/support";};   /* → beskedsystemet, ruten «Min konsulent» er forvalgt */
        const gen=document.getElementById("konsGenerisk");if(gen)gen.style.display="none"; /* undgå dobbelt-info */
    }catch(e){} /* offline/lokal test: det generelle kort vises bare */
}

/* ===================== Nyheds-administration (kun administratorer) ===================== */
async function renderNyAdmin(){
    const liste=document.getElementById("nyaListe"),adg=document.getElementById("nyaAdgang"),
        opret=document.getElementById("nyaOpretBoks");
    const sess=fbaSess();
    admMenuIndsaet("admMenuNyadm","nyheder");
    if(!sess||!sess.token){liste.innerHTML='<p style="color:var(--graa)">Kræver administrator-login.</p>';opret.style.display="none";return;}
    let j=null;
    try{j=await apiKald({a:"nyadmin",token:sess.token});}catch(e){}
    if(!j||!j.ok){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente — virker kun online.")+'</p>';return;}
    const aaben=!!j.aaben,erSoren=!!j.ejer,maaNy=j.maaNyheder===undefined?true:!!j.maaNyheder;
    /* rettigheds-kort */
    adg.hidden=false;
    adg.innerHTML='<div class="fba-kort" style="display:flex;gap:14px;align-items:center;flex-wrap:wrap;border-left:5px solid '+(aaben?'var(--groen)':'#f5b400')+'">'+
        '<span style="font-size:1.4rem">🔑</span>'+
        '<div style="flex:1;min-width:200px"><b>Admins kan oprette nyheder: '+(aaben?'<span style="color:#41630a">Ja</span>':'<span style="color:#a86b00">Nej — kun ejeren</span>')+'</b></div>'+
        (erSoren?'<button id="nyaAdgSkift" class="fba-done" style="border:0;border-radius:12px;padding:10px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">'+(aaben?'Luk for andre admins':'Åbn for alle admins')+'</button>'
            :'<span style="color:var(--graa);font-size:.8rem">🔐 Kun sorenadm kan ændre</span>')+
        '</div>';
    const skift=document.getElementById("nyaAdgSkift");
    if(skift)skift.onclick=async function(){
        this.disabled=true;
        try{const r=await apiKald({a:"nyadgang",token:sess.token,aaben:aaben?0:1});if(r&&r.ok)renderNyAdmin();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
    /* opret-boks: lås hvis lukket og man ikke er sorenadm */
    const maaOprette=(aaben||erSoren)&&maaNy;
    opret.style.opacity=maaOprette?"1":".5";
    const udgivKnap=document.getElementById("nyaUdgiv"),fortrydKnap=document.getElementById("nyaFortryd");
    function nyaRyd(){   /* tøm formularen og forlad redigerings-tilstand */
        ["nyaTitel","nyaTekst","nyaLink","nyaPub","nyaMaalListe"].forEach(i=>{document.getElementById(i).value="";});
        document.getElementById("nyaKat").value="nyt";
        document.getElementById("nyaLinkTxt").value="laes";
        document.getElementById("nyaMaal").value="borgere";   /* standard: borger-nyheder rammer ikke medarbejderne */
        document.getElementById("nyaMaalListe").hidden=true;
        delete udgivKnap.dataset.redig;
        udgivKnap.textContent="📣 Udgiv nyheden";
        document.getElementById("nyaOverskrift").textContent="✍️ Skriv en nyhed";
        fortrydKnap.hidden=true;
    }
    fortrydKnap.onclick=nyaRyd;
    document.getElementById("nyaMaal").onchange=function(){document.getElementById("nyaMaalListe").hidden=this.value!=="liste";};
    udgivKnap.disabled=!maaOprette;
    udgivKnap.onclick=async function(){
        const kat=document.getElementById("nyaKat").value,
            titel=document.getElementById("nyaTitel").value.trim(),
            tekst=document.getElementById("nyaTekst").value.trim(),
            link=document.getElementById("nyaLink").value.trim(),
            linktxt=document.getElementById("nyaLinkTxt").value;
        if(!titel||!tekst){alert("Udfyld både overskrift og tekst.");return;}
        this.disabled=true;
        try{
            const redig=this.dataset.redig;   /* sat af ✏️ Redigér — ellers oprettes ny */
            const kald={a:redig?"nyredi":"nyopret",token:sess.token,kat:kat,titel:titel,tekst:tekst,link:link,linktxt:linktxt,
                publicer:document.getElementById("nyaPub").value||"",
                maal:document.getElementById("nyaMaal").value,
                liste:document.getElementById("nyaMaalListe").value.trim()};
            if(redig)kald.id=redig;
            const r=await apiKald(kald);
            if(r&&r.ok){nyaRyd();renderNyAdmin();}
            else alert((r&&r.fejl)||"Kunne ikke gemme");
        }catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        this.disabled=false;
    };
    /* listen */
    const alle=(j.nyheder||[]).slice().sort((a,b)=>(b.ts||0)-(a.ts||0));
    liste.innerHTML=alle.length?alle.map(n=>{
        const kat=NYKAT[n.kat]||["📢","Nyt"];
        const st=n.status||"aktiv";
        const planlagt=n.publicer&&n.publicer*1000>Date.now();
        const maalTxt={borgere:"🎯 kun borgere",medarb:"🎯 kun medarbejdere",liste:"🎯 udvalgte borgere"}[n.maal]||"";
        const ekstraBadges=(planlagt?'<span class="fba-side" style="background:rgba(60,180,229,.16);color:var(--moerk)">⏳ planlagt '+fbaDato(n.publicer)+'</span>':'')+
            (maalTxt?'<span class="fba-side">'+maalTxt+'</span>':'');
        const stBadge=(st==="aktiv"?'<span class="fba-side" style="background:rgba(147,213,0,.2);color:#41630a">Aktiv</span>'
            :st==="skjult"?'<span class="fba-side" style="background:rgba(30,55,76,.1);color:var(--moerk)">Skjult</span>'
                :'<span class="fba-side" style="background:rgba(245,180,0,.18);color:#8a6400">Arkiv</span>')+ekstraBadges;
        const d=new Date((n.ts||0)*1000),p=x=>(x<10?"0":"")+x;
        const rd=n.rts?new Date(n.rts*1000):null;
        return '<div class="fba-kort"><div class="fba-top">'+
            '<span class="n-tag '+fbaEsc(n.kat||"nyt")+'">'+kat[0]+' '+kat[1]+'</span>'+stBadge+
            '<span>'+p(d.getDate())+"."+p(d.getMonth()+1)+"."+d.getFullYear()+' · af '+fbaEsc(n.af||"")+
            (rd?' · ✏️ rettet '+p(rd.getDate())+"."+p(rd.getMonth()+1)+' af '+fbaEsc(n.raf||""):'')+'</span></div>'+
            '<div style="font-weight:800;margin:4px 0">'+fbaEsc(n.titel||"")+'</div>'+
            '<div class="fba-txt" style="font-size:.9rem">'+fbaEsc(n.tekst||"")+'</div>'+
            (n.link?'<div style="color:var(--graa);font-size:.8rem;margin-top:4px">🔗 '+fbaEsc(n.link)+' · knap: «'+(n.linktxt==="tryk"?"Tryk her":"Læs mere")+'»</div>':'')+
            '<div class="fba-handling" style="margin-top:10px">'+
            (maaOprette?'<button class="fba-done nya-redi" data-id="'+fbaEsc(n.id)+'">✏️ Redigér</button>':'')+
            (st!=="aktiv"?'<button class="fba-done nya-st" data-id="'+fbaEsc(n.id)+'" data-st="aktiv">▶ Gør aktiv</button>':'')+
            (st!=="skjult"?'<button class="fba-done nya-st" data-id="'+fbaEsc(n.id)+'" data-st="skjult">🙈 Skjul</button>':'')+
            (st!=="arkiv"?'<button class="fba-done nya-st" data-id="'+fbaEsc(n.id)+'" data-st="arkiv">🗄️ Læg i arkiv</button>':'')+
            '</div></div>';
    }).join(""):'<p style="color:var(--graa)">Ingen nyheder endnu — skriv den første ovenfor ✍️</p>';
    liste.querySelectorAll(".nya-st").forEach(b=>{
        b.onclick=async function(){
            this.disabled=true;
            try{const r=await apiKald({a:"nystatus",token:sess.token,id:this.getAttribute("data-id"),status:this.getAttribute("data-st")});
                if(r&&r.ok)renderNyAdmin();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    /* ✏️ Redigér: læg nyheden op i formularen — Udgiv-knappen bliver «Gem ændringer» */
    liste.querySelectorAll(".nya-redi").forEach(b=>{
        b.onclick=function(){
            const n=alle.find(x=>x.id===this.getAttribute("data-id"));if(!n)return;
            document.getElementById("nyaKat").value=n.kat||"nyt";
            document.getElementById("nyaTitel").value=n.titel||"";
            document.getElementById("nyaTekst").value=n.tekst||"";
            document.getElementById("nyaLink").value=n.link||"";
            document.getElementById("nyaLinkTxt").value=n.linktxt==="tryk"?"tryk":"laes";
            if(n.publicer&&n.publicer*1000>Date.now()){
                const d=new Date(n.publicer*1000),p=x=>(x<10?"0":"")+x;
                document.getElementById("nyaPub").value=d.getFullYear()+"-"+p(d.getMonth()+1)+"-"+p(d.getDate())+"T"+p(d.getHours())+":"+p(d.getMinutes());
            }else document.getElementById("nyaPub").value="";
            document.getElementById("nyaMaal").value=n.maal||"alle";
            document.getElementById("nyaMaalListe").value=n.liste||"";
            document.getElementById("nyaMaalListe").hidden=(n.maal||"alle")!=="liste";
            udgivKnap.dataset.redig=n.id;
            udgivKnap.textContent="💾 Gem ændringer";
            document.getElementById("nyaOverskrift").textContent="✏️ Redigér nyheden";
            fortrydKnap.hidden=false;
            document.getElementById("nyaOpretBoks").scrollIntoView({behavior:"smooth",block:"start"});
        };
    });
}

/* ===================== Feedback-overblik (kun administratorer) ===================== */
let fbaData=null,fbaG="side";
const fbaAabne=new Set();   /* hvilke grupper står åbne (huskes ved genrender) */
function fbaEsc(t){return String(t==null?"":t).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));}
function fbaDato(ts){const d=new Date(ts*1000),p=n=>(n<10?"0":"")+n;
    return p(d.getDate())+"."+p(d.getMonth()+1)+"."+d.getFullYear()+" "+p(d.getHours())+":"+p(d.getMinutes());}
function fbaSess(){try{return JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){return null;}}
/* Vis en midlertidig login-kode tydeligt (ved opret + nulstil), så den kan kopieres og sendes til borgeren. */
function visMidlKode(navn,u,kode){
    var besked="Hej "+(navn||"")+"\n\nDit login til BPA-universet:\nBrugernavn: "+u+"\nMidlertidig kode: "+kode+"\n\nFørste gang logger du ind med den midlertidige kode og vælger derefter dit eget. Log ind på "+location.origin+"/\n\nVenlig hilsen\nHandicapformidlingen";
    var ov=document.createElement("div");
    ov.style.cssText="position:fixed;inset:0;z-index:99997;background:rgba(30,55,76,.5);display:flex;align-items:center;justify-content:center;padding:20px";
    ov.innerHTML='<div style="width:min(440px,100%);background:#fff;border-radius:22px;box-shadow:0 30px 80px rgba(0,0,0,.4);font-family:var(--font);overflow:hidden">'+
        '<div style="background:linear-gradient(135deg,#93d500,#5f8a00);color:#fff;padding:20px 24px"><div style="font-size:1.7rem;line-height:1">🔑</div><h2 style="margin:6px 0 0;font-size:1.2rem">Midlertidig kode til '+fbaEsc(navn||u)+'</h2></div>'+
        '<div style="padding:20px 24px">'+
        '<p style="margin:0 0 4px;color:var(--graa);font-size:.9rem">Kopiér koden og send den til borgeren. Den vises <b>kun her</b> — kan ikke hentes frem igen (men du kan altid lave en ny med «Nulstil»).</p>'+
        '<div style="font-family:monospace;font-size:1.6rem;font-weight:800;letter-spacing:.14em;text-align:center;background:#f4f8fb;border:2px dashed #cfe0ec;border-radius:12px;padding:14px;margin:12px 0">'+fbaEsc(kode)+'</div>'+
        '<div style="font-size:.85rem;color:var(--moerk);margin:0 0 14px">Brugernavn: <b>'+fbaEsc(u)+'</b></div>'+
        '<div style="display:flex;gap:8px;flex-wrap:wrap">'+
        '<button id="mkKopiKode" style="flex:1;min-width:130px;border:0;border-radius:12px;padding:12px;cursor:pointer;font-family:var(--font);font-weight:800;background:var(--moerk);color:#fff">📋 Kopiér kode</button>'+
        '<button id="mkKopiBesked" style="flex:1;min-width:130px;border:0;border-radius:12px;padding:12px;cursor:pointer;font-family:var(--font);font-weight:800;background:var(--groen);color:#123">✉️ Kopiér besked</button>'+
        '</div>'+
        '<button id="mkLuk" style="width:100%;margin-top:10px;border:1px solid rgba(30,55,76,.2);background:#fff;border-radius:12px;padding:11px;cursor:pointer;font-family:var(--font);color:var(--graa)">Luk</button>'+
        '</div></div>';
    document.body.appendChild(ov);
    function kopi(txt,knap,ok){var t=knap.textContent;try{navigator.clipboard.writeText(txt).then(function(){knap.textContent="✓ "+ok;setTimeout(function(){knap.textContent=t;},1500);},function(){window.prompt("Kopiér manuelt:",txt);});}catch(e){window.prompt("Kopiér manuelt:",txt);}}
    ov.querySelector("#mkKopiKode").onclick=function(){kopi(kode,this,"Kopieret");};
    ov.querySelector("#mkKopiBesked").onclick=function(){kopi(besked,this,"Kopieret");};
    ov.querySelector("#mkLuk").onclick=function(){ov.remove();};
    ov.addEventListener("click",function(e){if(e.target===ov)ov.remove();});
}
async function hentFbAdmin(){
    const liste=document.getElementById("fbaListe"),bar=document.getElementById("fbaBar");
    const sess=fbaSess();
    if(!sess||!sess.token){bar.hidden=true;liste.innerHTML='<p style="color:var(--graa)">Denne side kræver administrator-login — log ud og log ind med dit admin-brugernavn på forsiden.</p>';return;}
    hentChatStatus();   /* 🤖 AI-chat til/fra-knappen øverst */
    renderLiveChat();   /* 💬 live-chat-panelet — vises SAMMEN med feedback */
    liste.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    try{
        /* univers-feedback OG læringshubbens data hentes samtidig — alt samles ét sted */
        const [j,h]=await Promise.all([
            apiKald({a:"ufbdata",token:sess.token}),
            apiKald({a:"admdata",token:sess.token}).catch(()=>null)
        ]);
        if(!j||!j.ok){bar.hidden=true;liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
        fbaData=j.feedback||[];
        fbaLaase=j.laase||{};
        fbaHub=(h&&h.ok)?{brugere:h.brugere||[],feedback:h.feedback||[]}:null;
        /* hub-feedback beriges så den kan flettes ind i feedback-visningerne — inkl. evt. svar (spejl) */
        if(fbaHub)fbaHub.feedback.forEach(f=>{
            f.kilde="hub";f.side="🎓 Læringshub"+(f.mod?" · "+f.mod:"");
            f.svar=(j.hubsvar&&j.hubsvar[f.id])||[];
        });
        bar.hidden=false;renderFba();
        renderDigest();   /* ☀️-panelet kan nu også tælle feedback med */
        startLaasPuls();
    }catch(e){liste.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — kræver serveren (virker ikke lokalt uden PHP).</p>';}
}
let fbaHub=null,fbaLaase={},fbaLaasTimer=null;
/* puls: opdatér «✍️ … er ved at svare»-mærkerne hvert 15. sek. — uden reload,
   og uden at genopbygge listen mens man selv sidder og skriver et svar */
function startLaasPuls(){
    if(fbaLaasTimer)clearInterval(fbaLaasTimer);
    fbaLaasTimer=setInterval(async ()=>{
        if(location.hash.indexOf("feedback-admin")<0){clearInterval(fbaLaasTimer);fbaLaasTimer=null;return;}
        const sess=fbaSess();if(!sess)return;
        try{
            const j=await apiKald({a:"ufblaasstatus",token:sess.token});
            if(!(j&&j.ok))return;
            const nu=JSON.stringify(j.laase||{});
            if(nu===JSON.stringify(fbaLaase))return;   /* intet ændret */
            fbaLaase=j.laase||{};
            const aktiv=document.activeElement;
            const skriver=aktiv&&aktiv.tagName==="TEXTAREA"&&aktiv.value.trim();
            if(!skriver&&(fbaG==="side"||fbaG==="ny"||fbaG==="borger"))renderFba();
        }catch(e){}
    },15000);
}
/* --- 🤖 AI-chat åben/lukket (knappen der erstatter fil-redigering) --- */
async function hentChatStatus(){
    const boks=document.getElementById("fbaChat");
    const sess=fbaSess();if(!boks||!sess)return;
    try{
        const j=await apiKald({a:"chatlive",token:sess.token});
        if(j&&j.ok)renderChatToggle(j);else boks.hidden=true;
    }catch(e){boks.hidden=true;}
}
function renderChatToggle(st){
    const boks=document.getElementById("fbaChat");
    const aaben=!!st.live;
    boks.innerHTML='<div class="fba-kort" style="display:flex;gap:14px;align-items:center;flex-wrap:wrap;border-left:5px solid '+(aaben?'var(--groen)':'#f5b400')+'">'+
        '<span style="font-size:1.5rem">🤖</span>'+
        '<div style="flex:1;min-width:220px"><b>AI-chat for borgerne: '+(aaben?'<span style="color:#41630a">Åben</span>':'<span style="color:#a86b00">Lukket — testtilstand</span>')+'</b>'+
        '<div style="color:var(--graa);font-size:.85rem">'+
        (st.noegleOk?(aaben?'Borgerne får AI-svar i chatten. Spørgsmål og svar logges til kvalitetskontrol.':'Kun administratorer møder AI\'en — borgerne får den almindelige hjælpe-bot.')
            :'⚠️ API-nøglen mangler i chat-noegle.php på serveren — AI\'en kan ikke svare, uanset denne kontakt.')+
        '</div></div>'+
        (st.ejer
            ?'<button id="chatSkift" class="'+(aaben?'fba-done':'fba-send')+'" style="border:0;border-radius:12px;padding:11px 18px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.87rem">'+(aaben?'Luk for borgere':'Åbn for borgere')+'</button>'
            :'<span style="color:var(--graa);font-size:.8rem">🔐 Kun ejeren kan ændre</span>')+
        '</div>';
    boks.hidden=false;
    const skiftKnap=document.getElementById("chatSkift");
    if(!skiftKnap)return;   /* øvrige admins ser kun status */
    skiftKnap.onclick=async function(){
        if(!confirm(aaben
            ?"Luk AI-chatten for borgerne?\nDe får den almindelige hjælpe-bot i stedet — ingen fejl, bare enklere svar."
            :"Åbn AI-chatten for alle borgere?"))return;
        this.disabled=true;
        try{
            const j=await apiKald({a:"chatlive",token:fbaSess().token,live:aaben?0:1});
            if(j&&j.ok)renderChatToggle(j);
            else{this.disabled=false;alert((j&&j.fejl)||"Kunne ikke gemme");}
        }catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
}
function fbaKortHTML(b){
    const erHub=b.kilde==="hub";
    const rate=b.rate==="op"?"👍":b.rate==="ned"?"👎":"";
    const svar=(b.svar||[]).map(sv=>{
        const borger=sv.fra==="borger";
        const navn=borger?(b.n||"Borgeren"):(sv.afVis||sv.af||"Medarbejder");
        const fotoAf=borger?(b.u||""):(sv.af||"");
        return '<div style="display:flex;gap:9px;margin:8px 0 0">'+
            hfAvatarHTML(fotoAf,navn)+
            '<div style="flex:1;min-width:0">'+
            '<div style="font-size:.76rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(navn)+'</b> · '+fbaDato(sv.ts||0)+'</div>'+
            '<div style="white-space:pre-wrap;word-break:break-word;font-size:.9rem;margin-top:1px">'+fbaEsc(sv.svar||"")+'</div>'+
            '</div></div>';
    }).join("");
    const nytFraBorger=!b.done&&b.svar&&b.svar.length&&b.svar[b.svar.length-1].fra==="borger";
    const sla=slaOver(b);
    return '<div class="fba-kort'+(b.done?' haandteret':'')+'"'+(sla?' style="border-left:5px solid var(--roed)"':'')+' data-id="'+fbaEsc(b.id)+'" data-kilde="'+(erHub?'hub':'univers')+'">'+
        '<div class="fba-top"><span class="fba-side">'+fbaEsc(b.side||"?")+'</span>'+
        (sla?'<span class="fba-side" style="background:rgba(231,0,76,.14);color:var(--roed)">⏰ '+sla+' hverdage uden svar</span>':'')+
        '<span class="fba-hvem" data-bku="'+fbaEsc(b.u||"")+'" title="Åbn borger-kortet">'+fbaEsc(b.n||"")+' ('+fbaEsc(b.u||"?")+')</span>'+
        '<span>'+fbaDato(b.ts)+'</span>'+
        (nytFraBorger?'<span class="fba-side" style="background:rgba(231,0,76,.12);color:var(--roed)">💬 nyt svar fra borgeren</span>':'')+
        (b.done?'<span>✅ håndteret af '+fbaEsc(b.doneAf||"")+'</span>':'')+'</div>'+
        '<div class="fba-txt">'+(rate?rate+" ":"")+fbaEsc(b.txt||"")+'</div>'+(b.filer&&b.filer.length?supFilerHTML(b.filer,false):'')+svar+
        (function(){
            const laas=fbaLaase[b.id];
            const mig=(fbaSess()||{}).u;
            const laastAfAnden=laas&&laas.af!==mig;
            let midt;
            if(erHub&&(!b.u||b.u==="faelles"))
                midt='<span style="flex:1;color:var(--graa);font-size:.8rem;align-self:center">Afgivet uden login — svar kan ikke leveres til borgeren</span>';
            else if(laastAfAnden)
                midt='<span class="fba-laas">✍️ '+fbaEsc(laas.af)+' er ved at svare på denne …</span>';
            else
                midt='<textarea id="fbaTa-'+fbaEsc(b.id)+'" placeholder="Skriv et svar til borgeren … (/ = standardsvar)"></textarea>'+
                    hfEmojiKnap("fbaTa-"+b.id)+
                    '<button class="fba-ai" title="Lav et AI-udkast ud fra vidensbasen — du redigerer og godkender selv" style="border:0;background:rgba(60,180,229,.14);color:var(--moerk);border-radius:10px;padding:0 12px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem">✍️ AI</button>'+
                    '<button class="fba-send">Send svar</button>';
            return '<div class="fba-handling">'+midt+
                '<button class="fba-done">'+(b.done?'Genåbn':'✓ Håndteret')+'</button>'+
                '</div></div>';
        })();
}
/* --- borger-adgang (kodeords-status + nulstil) --- */
let fbaAdgang=null,fbaSoeg="";
async function renderAdgang(){
    const liste=document.getElementById("fbaListe");
    if(!fbaAdgang){
        liste.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
        try{
            const j=await apiKald({a:"bpwstatus",token:fbaSess().token});
            if(!j||!j.ok){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
            fbaAdgang=j.borgere||[];
        }catch(e){liste.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — virker kun online.</p>';return;}
    }
    const q=fbaSoeg.toLowerCase();
    /* OBS: numeriske brugernavne (200349) ankommer som TAL fra serveren — altid String() før tekst-søgning */
    const rows=fbaAdgang.filter(b=>!q||String(b.u).toLowerCase().indexOf(q)>=0||String(b.n||"").toLowerCase().indexOf(q)>=0);
    const eget=fbaAdgang.filter(b=>b.eget).length;
    liste.innerHTML=
        '<div class="fba-top" style="margin-bottom:14px"><input id="adgSoeg" type="text" placeholder="Søg navn eller brugernavn …" value="'+fbaEsc(fbaSoeg)+'" '+
        'style="flex:1;min-width:220px;border:2px solid #dbe6ee;border-radius:12px;padding:9px 14px;font-family:var(--font);font-size:.9rem;outline:none">'+
        '<span>'+eget+' af '+fbaAdgang.length+' har valgt eget kodeord</span></div>'+
        (rows.length?rows.map(b=>{
            const status=b.eget?'🟢 Eget kodeord':(b.reset?'🟡 Nulstillet — venter på nyt':'⚪ Afventer første login');
            const sidst=b.sidst?('Sidst set '+fbaDato(b.sidst)):'Aldrig logget ind';
            return '<div class="fba-kort"><div class="fba-top" style="margin:0">'+
                '<span class="fba-hvem" data-bku="'+fbaEsc(b.u)+'" title="Åbn borger-kortet">'+fbaEsc(b.n||"")+' ('+fbaEsc(b.u)+')</span>'+
                (/^20\d{4}$/.test(b.u)?'':'<span class="fba-side">🏢 Internt personale</span>')+
                '<span>'+status+'</span><span>'+sidst+'</span>'+
                '<button class="fba-done adg-reset" data-u="'+fbaEsc(b.u)+'" data-n="'+fbaEsc(b.n||"")+'" style="margin-left:auto">🔑 Nulstil kode</button>'+
                '</div></div>';
        }).join(""):'<p style="color:var(--graa)">Ingen match.</p>');
    /* søgefeltet lyttes via delegation på fbaListe (se soegDelegation) — overlever alle gen-tegninger */
    liste.querySelectorAll(".adg-reset").forEach(k=>{
        k.onclick=async function(){
            const u=this.getAttribute("data-u"),nvn=this.getAttribute("data-n")||"";
            if(!confirm("Lav en ny midlertidig kode til "+u+"?\nDen gamle kode holder op med at virke, og du får den nye vist til at sende til borgeren."))return;
            this.disabled=true;
            try{
                const j=await apiKald({a:"bpwreset",token:fbaSess().token,u:u});
                if(j&&j.ok){const b=fbaAdgang.find(x=>x.u===u);if(b){b.eget=0;b.reset=1;}renderAdgang();if(j.kode)visMidlKode(nvn,u,j.kode);}
                else{this.disabled=false;alert((j&&j.fejl)||"Kunne ikke nulstille");}
            }catch(e){this.disabled=false;alert("Kunne ikke nulstille — tjek forbindelsen.");}
        };
    });
}
/* --- konsulent-styring (skift/fjern borgerens faste konsulent) --- */
let fbaKons=null,konsSoeg="";
function adgangsBorgere(medInterne){
    const ud=[];
    const raa=(typeof window.ADGANG_TEKST==="string")?window.ADGANG_TEKST.split("\n"):[];
    raa.forEach(l=>{l=l.trim();if(!l||l[0]==="#")return;
        const k=l.indexOf(":");const kode=(k<0?l:l.slice(0,k)).trim();
        const erBorger=/^20\d{4}$/.test(kode);
        if(!erBorger&&!medInterne)return; /* interne (500xxx, navne-koder) kun når de bedes om */
        ud.push({u:kode.toLowerCase(),n:k<0?"":l.slice(k+1).trim(),intern:!erBorger});});
    return ud;
}
async function renderKons(){
    const liste=document.getElementById("fbaListe");
    if(!fbaKons){
        liste.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
        try{
            const j=await apiKald({a:"konsdata",token:fbaSess().token});
            if(!j||!j.ok){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
            fbaKons={kat:j.katalog||{},bor:j.borgere||{}};
        }catch(e){liste.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — virker kun online.</p>';return;}
    }
    const alle=adgangsBorgere(true); /* borgere + interne (interne har Søren som demo-standard) */
    const maaK=!lcaData||!lcaData.ret||!!lcaData.ret.konsulent;   /* kræver leder-niveau (serveren håndhæver også) */
    const q=konsSoeg.toLowerCase();
    const rows=alle.filter(b=>!q||String(b.u).toLowerCase().indexOf(q)>=0||String(b.n||"").toLowerCase().indexOf(q)>=0);
    const medKons=alle.filter(b=>fbaKons.bor[b.u]).length;
    const navne=Object.keys(fbaKons.kat).sort((a,b)=>a.localeCompare(b,"da"));
    const VIS=80;
    liste.innerHTML=
        '<div class="fba-top" style="margin-bottom:14px"><input id="konsSoegFelt" type="text" placeholder="Søg navn eller brugernavn …" value="'+fbaEsc(konsSoeg)+'" '+
        'style="flex:1;min-width:220px;border:2px solid #dbe6ee;border-radius:12px;padding:9px 14px;font-family:var(--font);font-size:.9rem;outline:none">'+
        '<span>'+medKons+' af '+alle.length+' borgere har en konsulent</span>'+
        (maaK?'':'<span class="fba-side" style="background:rgba(30,55,76,.08)">🔐 din rolle kan kun se — skift kræver leder-niveau</span>')+'</div>'+
        rows.slice(0,VIS).map(b=>{
            const valgt=fbaKons.bor[b.u]||"";
            return '<div class="fba-kort"><div class="fba-top" style="margin:0;align-items:center">'+
                '<span class="fba-hvem" data-bku="'+fbaEsc(b.u)+'" title="Åbn borger-kortet">'+fbaEsc(b.n||"")+' ('+fbaEsc(b.u)+')</span>'+
                (b.intern?'<span class="fba-side">🏢 Internt personale</span>':'')+
                '<select class="kons-vaelg" data-u="'+fbaEsc(b.u)+'"'+(maaK?'':' disabled')+' style="margin-left:auto;border:2px solid #dbe6ee;border-radius:10px;padding:8px 10px;font-family:var(--font);font-size:.88rem;max-width:280px'+(maaK?'':';opacity:.55')+'">'+
                '<option value="">— ingen konsulent —</option>'+
                navne.map(n=>'<option value="'+fbaEsc(n)+'"'+(n===valgt?' selected':'')+'>'+fbaEsc(n)+'</option>').join("")+
                '</select><span class="kons-ok" style="width:22px;text-align:center"></span>'+
                '</div></div>';
        }).join("")+
        (rows.length>VIS?'<p style="color:var(--graa)">… og '+(rows.length-VIS)+' flere — brug søgefeltet for at finde en bestemt borger.</p>':'')+
        (rows.length?'':'<p style="color:var(--graa)">Ingen match.</p>');
    /* søgefeltet lyttes via delegation på fbaListe (se soegDelegation) — overlever alle gen-tegninger */
    liste.querySelectorAll(".kons-vaelg").forEach(sel=>{
        sel.onchange=async function(){
            const u=this.getAttribute("data-u"),navn=this.value,ok=this.parentElement.querySelector(".kons-ok");
            this.disabled=true;ok.textContent="⏳";
            try{
                const j=await apiKald({a:"konssaet",token:fbaSess().token,u:u,navn:navn});
                if(j&&j.ok){
                    if(navn)fbaKons.bor[u]=navn;else delete fbaKons.bor[u];
                    ok.textContent="✔";setTimeout(()=>{ok.textContent="";},1800);
                }else{ok.textContent="✖";alert((j&&j.fejl)||"Kunne ikke gemme");}
            }catch(e){ok.textContent="✖";alert("Kunne ikke gemme — tjek forbindelsen.");}
            this.disabled=false;
        };
    });
}
/* søge-delegation: handlers på CONTAINEREN, ikke på felterne — så virker søgning
   uanset hvor mange gange listen gen-tegnes (wiring kan aldrig gå tabt i en race) */
(function soegDelegation(){
    const fl=document.getElementById("fbaListe");
    if(!fl)return;
    fl.addEventListener("input",e=>{
        const t=e.target;
        if(!t||!t.id)return;
        if(t.id==="adgSoeg"){
            fbaSoeg=t.value;const pos=t.selectionStart;
            renderAdgang().then(()=>{const s2=document.getElementById("adgSoeg");if(s2){s2.focus();try{s2.setSelectionRange(pos,pos);}catch(x){}}});
        }else if(t.id==="konsSoegFelt"){
            konsSoeg=t.value;const pos=t.selectionStart;
            renderKons().then(()=>{const s2=document.getElementById("konsSoegFelt");if(s2){s2.focus();try{s2.setSelectionRange(pos,pos);}catch(x){}}});
        }
    });
})();
function renderFba(){
    if(fbaG==="adgang"){renderAdgang();return;}
    if(fbaG==="kons"){renderKons();return;}
    if(fbaG==="hub"){renderHubStat();return;}
    if(fbaG==="chat"){renderChatSamling();return;}
    if(fbaG==="stat"){renderStat();return;}
    if(fbaG==="fs"){renderFsAdmin();return;}
    const liste=document.getElementById("fbaListe");
    const visH=document.getElementById("fbaVisH").checked;
    const samlet=(fbaData||[]).concat(fbaHub?fbaHub.feedback:[]);  /* univers + læringshub — alt ét sted */
    let data=samlet.filter(b=>visH||!b.done);
    if(!data.length){liste.innerHTML='<p style="color:var(--graa)">Ingen '+(visH?'':'åbne ')+'beskeder 🎉</p>';return;}
    let html="";
    /* 💬 nye svar fra borgere — trækkes ud af grupperne og fastgøres øverst, altid udfoldet */
    const nye=data.filter(b=>!b.done&&b.svar&&b.svar.length&&b.svar[b.svar.length-1].fra==="borger")
        .slice().sort((a,b)=>(b.svar[b.svar.length-1].ts||0)-(a.svar[a.svar.length-1].ts||0));
    const nyeIds=new Set(nye.map(b=>b.id));
    data=data.filter(b=>!nyeIds.has(b.id));
    const grundlag=samlet.filter(b=>!nyeIds.has(b.id));   /* grupper uden de fastgjorte */
    if(nye.length){
        html+='<div class="fba-nye"><div class="fba-nye-titel">💬 Nye svar fra borgere'+
            '<span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+nye.length+'</span></div>'+
            nye.map(fbaKortHTML).join("")+'</div>';
    }
    function boks(key,titel,gruppe){ /* sammenfoldelig gruppe — husker åben/lukket hen over genindlæsninger */
        const aabne=gruppe.filter(x=>!x.done).length;
        const kort=gruppe.filter(b=>visH||!b.done).slice().sort((a,b)=>b.ts-a.ts).map(fbaKortHTML).join("");
        if(!kort)return "";
        return '<details class="fba-boks"'+(fbaAabne.has(key)?" open":"")+' data-gr="'+fbaEsc(key)+'">'+
            '<summary>'+titel+'<span class="antal">'+aabne+' åbne · '+gruppe.length+' i alt</span></summary>'+kort+'</details>';
    }
    if(fbaG==="ny"){
        html+=data.slice().sort((a,b)=>b.ts-a.ts).map(fbaKortHTML).join("");
    }else if(fbaG==="side"){
        /* to niveauer: kilde → side */
        const kilder={"🌐 BPA-universet":{},"🎓 Læringshubben":{}};
        grundlag.forEach(b=>{
            const k=b.kilde==="hub"?"🎓 Læringshubben":"🌐 BPA-universet";
            const s=b.kilde==="hub"?(b.mod||"Uden emne"):(b.side||"Ukendt side");
            (kilder[k][s]=kilder[k][s]||[]).push(b);
        });
        Object.keys(kilder).forEach(k=>{
            const sider=kilder[k];const alleI=Object.values(sider).flat();
            if(!alleI.length)return;
            const indre=Object.keys(sider).sort((a,b)=>a.localeCompare(b,"da"))
                .map(s=>boks("s:"+k+":"+s,fbaEsc(s),sider[s])).join("");
            if(!indre)return;
            const aabne=alleI.filter(x=>!x.done).length;
            html+='<details class="fba-boks fba-kilde"'+(fbaAabne.has("k:"+k)?" open":"")+' data-gr="k:'+fbaEsc(k)+'">'+
                '<summary>'+k+'<span class="antal">'+aabne+' åbne · '+alleI.length+' i alt</span></summary>'+indre+'</details>';
        });
    }else{
        /* efter borger — foldet pr. borger */
        const grupper={};
        grundlag.forEach(b=>{const k=(b.n||"")+" ("+(b.u||"?")+")";(grupper[k]=grupper[k]||[]).push(b);});
        Object.keys(grupper).sort((a,b)=>a.localeCompare(b,"da")).forEach(k=>{
            html+=boks("b:"+k,"👤 "+fbaEsc(k),grupper[k]);
        });
    }
    liste.innerHTML=html||'<p style="color:var(--graa)">Ingen '+(visH?'':'åbne ')+'beskeder 🎉</p>';
    hfAvatarWire(liste);
    /* husk åben/lukket-tilstand */
    liste.querySelectorAll("details[data-gr]").forEach(d=>{
        d.addEventListener("toggle",()=>{const k=d.getAttribute("data-gr");if(d.open)fbaAabne.add(k);else fbaAabne.delete(k);});
    });
    /* svar + håndtér pr. kort */
    liste.querySelectorAll(".fba-kort").forEach(kort=>{
        const id=kort.getAttribute("data-id");
        const erHub=kort.getAttribute("data-kilde")==="hub";
        const post=(erHub?(fbaHub&&fbaHub.feedback||[]):(fbaData||[])).find(x=>x.id===id);
        /* reservér tråden når svar-feltet åbnes — hold låsen i live mens der skrives, frigiv hvis feltet forlades tomt */
        const taFelt=kort.querySelector("textarea");
        if(taFelt){
            let sidsteLaas=0;
            async function goerKrav(){
                sidsteLaas=Date.now();
                try{
                    const j=await apiKald({a:"ufblaas",token:fbaSess().token,id:id});
                    if(j&&j.ok===false&&j.laastAf){
                        alert("✍️ "+j.laastAf+" er allerede i gang med at svare på denne besked.");
                        hentFbAdmin();   /* genindlæs så låsen vises */
                    }
                }catch(e){}
            }
            taFelt.addEventListener("focus",goerKrav);
            taFelt.addEventListener("input",()=>{if(Date.now()-sidsteLaas>60000)goerKrav();}); /* forny låsen under skrivning */
            taFelt.addEventListener("blur",()=>{
                if(!taFelt.value.trim()){apiKald({a:"ufblaasop",token:fbaSess().token,id:id}).catch(()=>{});}
            });
            svarForslagOpsæt(taFelt,()=>((post&&post.n)||"").split(" ")[0]);   /* «/» = standardsvar */
        }
        const aiBtn=kort.querySelector(".fba-ai");
        if(aiBtn)aiBtn.onclick=async function(){
            this.disabled=true;this.textContent="⏳";
            try{
                const kontekst="Borgerens besked ("+((post&&post.side)||"")+"): "+((post&&post.txt)||"")+
                    ((post&&post.svar&&post.svar.length)?"\nHidtidig tråd:\n"+post.svar.map(sv=>((sv.fra==="borger"?"Borger: ":"Medarbejder: ")+sv.svar)).join("\n"):"");
                const j=await apiKald({a:"aiudkast",token:fbaSess().token,tekst:kontekst});
                if(j&&j.ok){const ta=kort.querySelector("textarea");if(ta){ta.value=j.udkast;ta.focus();}}
                else alert((j&&j.fejl)||"Kunne ikke lave udkast");
            }catch(e){alert("Kunne ikke lave udkast — tjek forbindelsen.");}
            this.disabled=false;this.textContent="✍️ AI";
        };
        const sendBtn=kort.querySelector(".fba-send");
        if(sendBtn)sendBtn.onclick=async function(){
            const ta=kort.querySelector("textarea"),tekst=ta.value.trim();
            if(!tekst){ta.focus();return;}
            this.disabled=true;
            try{
                const j=await apiKald({a:erHub?"ufbsvarhub":"ufbsvar",token:fbaSess().token,id:id,svar:tekst});
                if(j&&j.ok){post.svar=post.svar||[];post.svar.push({id:id,svar:tekst,af:fbaSess().u,afVis:(j.afVis||fbaSess().u),fra:"hf",ts:Math.floor(Date.now()/1000)});renderFba();}
                else{this.disabled=false;alert((j&&j.fejl)||"Kunne ikke sende");}
            }catch(e){this.disabled=false;alert("Kunne ikke sende — tjek forbindelsen.");}
        };
        kort.querySelector(".fba-done").onclick=async function(){
            this.disabled=true;
            try{
                const j=await apiKald({a:erHub?"fbdone":"ufbdone",token:fbaSess().token,id:id,done:post.done?0:1});
                if(j&&j.ok){post.done=post.done?0:1;post.doneAf=j.af;renderFba();try{hfAdmTael();}catch(e){}}
                else{this.disabled=false;alert((j&&j.fejl)||"Kunne ikke gemme");}
            }catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
        };
    });
}
/* --- 🎓 læringshub-statistik (samme data som hubbens eget adminoverblik) --- */
function renderHubStat(){
    const liste=document.getElementById("fbaListe");
    if(!fbaHub){liste.innerHTML='<p style="color:var(--graa)">Læringshubbens data kunne ikke hentes — prøv ⟳ Opdatér.</p>';return;}
    const brugere=(fbaHub.brugere||[]).slice().sort((a,b)=>b.sidst-a.sidst);
    const emner=new Set();brugere.forEach(b=>Object.keys(b.done||{}).forEach(m=>emner.add(m)));
    const N=Math.max(emner.size,10);
    const grp={"👥 Borgere":[],"🏢 Medarbejdere":[],"🧪 Interne testbrugere":[]};
    brugere.forEach(b=>{
        const u=String(b.u||"");
        (/^20\d{4}$/.test(u)?grp["👥 Borgere"]:/^5\d{5}$/.test(u)?grp["🧪 Interne testbrugere"]:grp["🏢 Medarbejdere"]).push(b);
    });
    const set=new Set(brugere.map(b=>String(b.u||"").toLowerCase()));
    const ikke=adgangsBorgere(true).filter(b=>!set.has(b.u));
    const faerdige=brugere.filter(b=>Object.keys(b.done||{}).length>=N).length;
    function raekke(b){
        const antal=Object.keys(b.done||{}).length;
        const pct=Math.round(antal/N*100);
        return '<div class="fba-top" style="margin:6px 0;padding:8px 12px;background:#fff;border-radius:12px;border:1px solid rgba(30,55,76,.06)">'+
            '<span class="fba-hvem">'+fbaEsc(b.n||"")+' ('+fbaEsc(b.u)+')</span>'+
            '<span>'+antal+' af '+N+' emner ('+pct+'%)</span>'+
            '<span style="margin-left:auto">sidst set '+fbaDato(b.sidst||0)+'</span></div>';
    }
    liste.innerHTML=
        '<div class="fba-top" style="margin-bottom:16px;gap:14px">'+
        '<span class="fba-side">'+brugere.length+' har logget ind</span>'+
        '<span class="fba-side">🏆 '+faerdige+' har gennemført alle '+N+' emner</span>'+
        '<span class="fba-side">😴 '+ikke.length+' har endnu ikke logget ind</span>'+
        '<a href="laeringshub/index.html" style="margin-left:auto;color:var(--blaa);font-weight:700;text-decoration:none">Hubbens detaljerede overblik →</a></div>'+
        Object.keys(grp).map(navn=>{
            const g=grp[navn];
            return '<details class="fba-kort" '+(navn==="👥 Borgere"?"open":"")+'><summary style="cursor:pointer;font-weight:800">'+navn+' — '+g.length+' logget ind</summary>'+
                (g.length?g.map(raekke).join(""):'<p style="color:var(--graa);margin:8px 0 0">Ingen endnu.</p>')+'</details>';
        }).join("")+
        '<details class="fba-kort"><summary style="cursor:pointer;font-weight:800">😴 Endnu ikke logget ind — '+ikke.length+'</summary>'+
        '<p style="color:var(--graa);font-size:.85rem;margin:8px 0">'+ikke.slice(0,400).map(b=>fbaEsc((b.n||"")+" ("+b.u+")")).join(" · ")+(ikke.length>400?" …":"")+'</p></details>';
}

/* ===================== visningsnavne: login («ceciadm») → navn («Cecilie») ===================== */
let ADM_VIS={},ADM_TITEL={};
function visA(login){return ADM_VIS[login]||login||"";}
function visT(login){return ADM_TITEL[login]||"";}   /* titel — vises kun i admin-vendte visninger */
function visTitelHTML(login){const t=visT(login);return t?' <span style="color:var(--graa);font-weight:400;font-size:.78em">· '+fbaEsc(t)+'</span>':"";}

/* ===================== 🗓 Årshjul (borger: diskret påmindelses-stribe) ===================== */
function hjulDatoKort(ts){const d=new Date(ts*1000),p=n=>(n<10?"0":"")+n;return p(d.getDate())+"."+p(d.getMonth()+1)+"."+d.getFullYear();}
function aabnHjulOversigt(punkter){
    let ov=document.getElementById("hjulOverlay");
    if(!ov){ov=document.createElement("div");ov.id="hjulOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:995;background:rgba(30,55,76,.45);display:flex;align-items:center;justify-content:center;padding:18px";
        ov.addEventListener("click",e=>{if(e.target===ov)ov.remove();});
        document.body.appendChild(ov);}
    ov.innerHTML='<div style="width:min(460px,100%);max-height:80vh;overflow-y:auto;background:#fff;border-radius:22px;padding:26px;font-family:var(--font);box-shadow:0 30px 80px rgba(0,0,0,.4)">'+
        '<div style="display:flex;align-items:center;margin-bottom:10px"><b style="font-size:1.05rem">🗓 Kommende datoer</b>'+
        '<button id="hjulLuk2" style="margin-left:auto;border:0;background:rgba(30,55,76,.08);border-radius:10px;width:32px;height:32px;cursor:pointer">✕</button></div>'+
        punkter.map(p=>'<div style="padding:10px 0;border-bottom:1px solid rgba(30,55,76,.07)">'+
            '<b>'+fbaEsc(p.titel)+'</b> <span style="color:var(--graa);font-size:.85rem">'+hjulDatoKort(p.ts)+' — '+(p.dage===0?"i dag":p.dage===1?"i morgen":"om "+p.dage+" dage")+'</span>'+
            (p.tekst?'<div style="color:#333;font-size:.88rem;margin-top:3px">'+fbaEsc(p.tekst)+'</div>':'')+'</div>').join("")+
        '</div>';
    document.getElementById("hjulLuk2").onclick=()=>ov.remove();
}
async function hentAarshjul(){
    const u=window.BPA_USER||"";
    if(!u||u==="faelles")return;
    let j=null;try{j=await apiKald({a:"aarshjul",u:u});}catch(e){return;}
    if(!(j&&j.ok))return;
    const p=(j.punkter||[]).filter(x=>{try{return sessionStorage.getItem("hjul_"+x.id+"_"+x.ts)!=="1";}catch(e){return true;}});
    if(!p.length)return;
    let b=document.getElementById("hjulBanner");
    if(!b){
        b=document.createElement("div");b.id="hjulBanner";
        b.style.cssText="max-width:1136px;width:calc(100% - 44px);margin:14px auto 0;box-sizing:border-box;border-radius:14px;background:linear-gradient(90deg,rgba(147,213,0,.18),rgba(60,180,229,.14));border:1px solid rgba(147,213,0,.4);padding:9px 18px;display:flex;gap:12px;align-items:center;font-size:.9rem;font-family:var(--font);flex-wrap:wrap";
        const nb=document.getElementById("nytBanner");
        if(nb&&nb.parentNode)nb.parentNode.insertBefore(b,nb.nextSibling);
        else{const m=document.querySelector("main");if(m&&m.parentNode)m.parentNode.insertBefore(b,m);}
    }
    const f=p[0],hv=f.dage===0?"i dag":f.dage===1?"i morgen":"om "+f.dage+" dage";
    b.innerHTML='<span>🗓</span><span><b>'+fbaEsc(f.titel)+'</b> — '+hv+'</span>'+
        (p.length>1?'<a href="#" id="hjulAlle" style="color:var(--blaa);font-weight:700;text-decoration:none">+'+(p.length-1)+' mere →</a>'
            :(f.tekst?'<a href="#" id="hjulAlle" style="color:var(--blaa);font-weight:700;text-decoration:none">Læs mere →</a>':''))+
        '<button id="hjulLuk" aria-label="Skjul" style="margin-left:auto;border:0;background:none;color:var(--graa);cursor:pointer;font-size:1rem">✕</button>';
    const alleKnap=document.getElementById("hjulAlle");
    if(alleKnap)alleKnap.onclick=e=>{e.preventDefault();aabnHjulOversigt(p);};
    document.getElementById("hjulLuk").onclick=()=>{
        p.forEach(x=>{try{sessionStorage.setItem("hjul_"+x.id+"_"+x.ts,"1");}catch(e){}});
        b.remove();
    };
}
setTimeout(hentAarshjul,1200);   /* efter login-gaten har sat brugeren */

/* ===================== 🌍 Fællesskabet (borger-forum) ===================== */
var fsStatus=null,fsListe=null,fsKat="",fsAaben=null,fsOpretVis=false;   /* var: routeren kan kalde synkront ved direkte load */
function fsTid(ts){
    const min=Math.floor((Date.now()/1000-ts)/60);
    if(min<1)return "lige nu";
    if(min<60)return min+" min. siden";
    const t=Math.floor(min/60);
    if(t<24)return t+(t===1?" time":" timer")+" siden";
    const d=Math.floor(t/24);
    return d<7?d+(d===1?" dag":" dage")+" siden":hjulDatoKort(ts);
}
function fsNavnHTML(navn,hf){
    return '<b>'+fbaEsc(navn)+'</b>'+(hf?' <span style="background:linear-gradient(120deg,var(--blaa),#1f8fc4);color:#fff;font-size:.68rem;font-weight:800;border-radius:999px;padding:2px 8px;vertical-align:1px">HF</span>'
        :(arguments[2]?' <span style="background:rgba(30,55,76,.1);color:var(--moerk);font-size:.68rem;font-weight:800;border-radius:999px;padding:2px 8px;vertical-align:1px">🏢 medarbejder</span>':''));
}
async function renderFaelles(){
    const wrap=document.getElementById("fsWrap");if(!wrap)return;
    const u=window.BPA_USER||"";
    if(!u||u==="faelles"){wrap.innerHTML='<p style="color:var(--graa)">Log ind for at komme ind i Fællesskabet.</p>';return;}
    if(!fsStatus){
        try{const j=await apiKald({a:"fsstatus",u:u});if(j&&j.ok)fsStatus=j;}catch(e){}
        if(!fsStatus){wrap.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — prøv igen om lidt.</p>';return;}
    }
    if(!fsStatus.adgang){wrap.innerHTML='<p style="color:var(--graa)">Fællesskabet er forbeholdt borgere med en BPA-ordning.</p>';return;}
    if(!fsStatus.accepteret){fsHusregler();return;}
    if(fsAaben){fsVisOpslag();return;}
    fsVisListe();
}
function fsHusregler(){
    const wrap=document.getElementById("fsWrap");
    wrap.innerHTML='<div class="fba-kort" style="max-width:560px;margin:0 auto">'+
        '<b style="font-size:1.05rem">Velkommen til Fællesskabet 💙</b>'+
        '<p style="color:#333;font-size:.9rem;margin:8px 0">Før du kommer ind, beder vi dig acceptere husreglerne:</p>'+
        '<ul style="font-size:.88rem;color:#333;padding-left:20px;line-height:1.7">'+
        '<li>Tal til andre, som du selv vil tales til — vi er her for at hjælpe hinanden.</li>'+
        '<li>Del ikke personfølsomme oplysninger — hverken dine egne eller andres (CPR, helbred, navngivne hjælpere).</li>'+
        '<li>Ingen reklamer eller salg.</li>'+
        '<li>Et automatisk filter tjekker alle opslag — markerer det noget, ser Handicapformidlingen på det, før det vises.</li>'+
        '<li>Fællesskabet erstatter ikke rådgivning — brug chatten eller din konsulent til konkrete sager.</li></ul>'+
        '<label style="display:block;font-size:.88rem;margin:12px 0 4px">Dit navn i Fællesskabet (andre borgere ser kun dette):</label>'+
        '<input id="fsNavnFelt" maxlength="40" value="'+fbaEsc(fsStatus.navn||"")+'" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:10px 14px;font-family:var(--font);font-size:.95rem;outline:none">'+
        '<button id="fsAcceptKnap" class="fba-send" style="width:100%;border:0;border-radius:12px;padding:13px;margin-top:12px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.95rem">Acceptér husreglerne og kom ind →</button></div>';
    document.getElementById("fsAcceptKnap").onclick=async function(){
        const navn=document.getElementById("fsNavnFelt").value.trim();
        if(navn.length<2){alert("Vælg et visningsnavn på mindst 2 tegn.");return;}
        this.disabled=true;
        try{const r=await apiKald({a:"fsaccept",u:window.BPA_USER,navn:navn});
            if(r&&r.ok){fsStatus.accepteret=1;fsStatus.navn=navn;renderFaelles();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
}
async function fsVisListe(){
    const wrap=document.getElementById("fsWrap");
    let j=null;
    try{j=await apiKald({a:"fsliste",u:window.BPA_USER,kat:fsKat});}catch(e){}
    if(!(j&&j.ok)){wrap.innerHTML='<p style="color:var(--roed)">Kunne ikke hente opslag.</p>';return;}
    fsListe=j.opslag||[];
    const kat=j.kategorier||{};
    let html='<div class="fba-bar">'+
        '<button class="fs-kat'+(fsKat===""?' aktiv':'')+'" data-k="">Alle</button>'+
        Object.keys(kat).map(k=>'<button class="fs-kat'+(k===fsKat?' aktiv':'')+'" data-k="'+k+'">'+kat[k]+'</button>').join("")+
        '<button id="fsNyKnap">✍️ Nyt opslag</button></div>';
    if(fsOpretVis){
        html+='<div class="fba-kort" style="border-left:5px solid var(--blaa)"><b>✍️ Nyt opslag</b>'+
            '<p style="color:var(--graa);font-size:.82rem;margin:4px 0 10px">Dit opslag vises med det samme, når det automatiske tjek er ok — ellers ser Handicapformidlingen på det først.</p>'+
            '<div style="display:grid;gap:10px">'+
            '<select id="fsOpKat" class="nya-felt">'+Object.keys(kat).map(k=>'<option value="'+k+'"'+(k===fsKat?' selected':'')+'>'+kat[k]+'</option>').join("")+'</select>'+
            '<input id="fsOpTitel" class="nya-felt" maxlength="100" placeholder="Overskrift">'+
            '<textarea id="fsOpTxt" class="nya-felt" rows="5" maxlength="4000" placeholder="Skriv dit opslag … del gerne erfaringer, men husk: ingen personfølsomme oplysninger."></textarea>'+
            '<div style="display:flex;gap:10px"><button id="fsOpSend" class="fba-send" style="border:0;border-radius:11px;padding:11px 18px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.9rem">Send til godkendelse</button>'+
            '<button id="fsOpNej" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.85rem">Annullér</button></div></div></div>';
    }
    html+=fsListe.length?fsListe.map(o=>{
        const afventer=o.status==="afventer",afvist=o.status==="afvist";
        return '<div class="fba-kort fs-kort" data-oid="'+o.oid+'" style="cursor:pointer'+(o.hf?';border-left:5px solid var(--blaa)':'')+(afventer||afvist?';opacity:.75':'')+'">'+
            '<div class="fba-top" style="margin-bottom:4px">'+fsNavnHTML(o.navn,o.hf,o.medarb)+
            '<span style="color:var(--graa);font-size:.8rem">'+fsTid(o.ts)+'</span>'+
            (afventer?'<span class="fba-side" style="background:rgba(245,180,0,.2);color:#8a6400">⏳ afventer godkendelse</span>':'')+
            (afvist?'<span class="fba-side" style="background:rgba(231,0,76,.14);color:var(--roed)">ikke godkendt</span>':'')+
            (o.laast?'<span class="fba-side">🔒 lukket</span>':'')+'</div>'+
            '<b style="font-size:1.02rem">'+fbaEsc(o.titel)+'</b>'+
            '<div style="color:#333;font-size:.9rem;margin-top:3px">'+fbaEsc(o.txt)+(o.txt.length>=400?" …":"")+'</div>'+
            '<div style="display:flex;gap:14px;margin-top:8px;color:var(--graa);font-size:.85rem"><span>👍 '+o.op+'</span><span>❤️ '+o.hjerte+'</span><span>💬 '+o.kommentarer+'</span></div></div>';
    }).join(""):'<p style="color:var(--graa)">Ingen opslag her endnu — bliv den første ✍️</p>';
    wrap.innerHTML=html;
    wrap.querySelectorAll(".fs-kat").forEach(b=>{b.onclick=()=>{fsKat=b.getAttribute("data-k");fsOpretVis=false;fsVisListe();};});
    document.getElementById("fsNyKnap").onclick=()=>{fsOpretVis=!fsOpretVis;fsVisListe();};
    if(fsOpretVis){
        document.getElementById("fsOpNej").onclick=()=>{fsOpretVis=false;fsVisListe();};
        document.getElementById("fsOpSend").onclick=async function(){
            const titel=document.getElementById("fsOpTitel").value.trim(),txt=document.getElementById("fsOpTxt").value.trim();
            if(!titel||!txt){alert("Udfyld både overskrift og tekst.");return;}
            this.disabled=true;
            try{const r=await apiKald({a:"fsopret",u:window.BPA_USER,kat:document.getElementById("fsOpKat").value,titel:titel,txt:txt});
                if(r&&r.ok){fsOpretVis=false;fsVisListe();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}
            catch(e){this.disabled=false;}
        };
    }
    wrap.querySelectorAll(".fs-kort").forEach(k=>{k.onclick=()=>{fsAaben=k.getAttribute("data-oid");fsVisOpslag();};});
}
async function fsVisOpslag(){
    const wrap=document.getElementById("fsWrap");
    let j=null;
    try{j=await apiKald({a:"fsopslag",u:window.BPA_USER,oid:fsAaben});}catch(e){}
    if(!(j&&j.ok)){fsAaben=null;fsVisListe();return;}
    const o=j.opslag,komm=j.kommentarer||[];
    wrap.innerHTML='<button id="fsTilbage" style="border:0;background:rgba(30,55,76,.08);color:var(--moerk);border-radius:10px;padding:8px 14px;cursor:pointer;font-family:var(--font);font-weight:700;margin-bottom:12px">‹ Alle opslag</button>'+
        '<div class="fba-kort"'+(o.hf?' style="border-left:5px solid var(--blaa)"':'')+'>'+
        '<div class="fba-top">'+fsNavnHTML(o.navn,o.hf,o.medarb)+'<span style="color:var(--graa);font-size:.8rem">'+fsTid(o.ts)+'</span>'+
        (o.laast?'<span class="fba-side">🔒 lukket for kommentarer</span>':'')+
        (o.status==="afventer"?'<span class="fba-side" style="background:rgba(245,180,0,.2);color:#8a6400">⏳ afventer godkendelse</span>':'')+
        '<button class="fs-anmeld" data-oid="'+o.oid+'" style="margin-left:auto;border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.78rem;text-decoration:underline">Anmeld</button></div>'+
        '<b style="font-size:1.1rem">'+fbaEsc(o.titel)+'</b>'+
        '<div style="color:#333;font-size:.93rem;margin-top:6px;white-space:pre-wrap">'+fbaEsc(o.txt)+'</div>'+
        (o.status==="aktiv"?'<div style="display:flex;gap:10px;margin-top:12px">'+
            '<button class="fs-reak" data-r="op" style="border:1px solid rgba(60,180,229,.4);background:#fff;border-radius:999px;padding:7px 14px;cursor:pointer;font-family:var(--font);font-size:.88rem">👍</button>'+
            '<button class="fs-reak" data-r="hjerte" style="border:1px solid rgba(231,0,76,.35);background:#fff;border-radius:999px;padding:7px 14px;cursor:pointer;font-family:var(--font);font-size:.88rem">❤️</button></div>':'')+
        '</div>'+
        '<h3 style="margin:20px 0 8px">💬 Kommentarer ('+komm.filter(x=>x.status==="aktiv").length+')</h3>'+
        (komm.map(k=>'<div class="fba-kort" style="padding:12px 16px'+(k.status==="afventer"?';opacity:.7':'')+'">'+
            '<div class="fba-top" style="margin-bottom:2px">'+fsNavnHTML(k.navn,k.hf,k.medarb)+'<span style="color:var(--graa);font-size:.78rem">'+fsTid(k.ts)+'</span>'+
            (k.status==="afventer"?'<span class="fba-side" style="background:rgba(245,180,0,.2);color:#8a6400">⏳</span>':'')+'</div>'+
            '<div style="font-size:.9rem;color:#333;white-space:pre-wrap">'+fbaEsc(k.txt)+'</div></div>').join("")||'<p style="color:var(--graa)">Ingen kommentarer endnu.</p>')+
        (o.status==="aktiv"&&!o.laast?'<div style="display:flex;gap:10px;margin-top:10px">'+
            '<input id="fsKomFelt" maxlength="2000" placeholder="Skriv en kommentar …" style="flex:1;border:2px solid #dbe6ee;border-radius:999px;padding:11px 16px;font-family:var(--font);font-size:.9rem;outline:none">'+
            '<button id="fsKomSend" class="fba-send" style="border:0;border-radius:50%;width:44px;height:44px;flex:none;cursor:pointer">➤</button></div>':'');
    document.getElementById("fsTilbage").onclick=()=>{fsAaben=null;fsVisListe();};
    wrap.querySelectorAll(".fs-reak").forEach(b=>{
        b.onclick=async ()=>{
            try{await apiKald({a:"fsreaktion",u:window.BPA_USER,oid:fsAaben,r:b.getAttribute("data-r")});
                b.style.background="rgba(147,213,0,.18)";}catch(e){}
        };
    });
    wrap.querySelectorAll(".fs-anmeld").forEach(b=>{
        b.onclick=async e=>{
            e.stopPropagation();
            const grund=(window.prompt("Hvad er der galt med opslaget? (sendes til Handicapformidlingen)")||"").trim();
            if(!grund)return;
            try{const r=await apiKald({a:"fsanmeld",u:window.BPA_USER,oid:fsAaben,grund:grund});
                if(r&&r.ok)alert("Tak — vi kigger på det hurtigst muligt.");}catch(e2){}
        };
    });
    const ks=document.getElementById("fsKomSend"),kf=document.getElementById("fsKomFelt");
    if(ks)ks.onclick=async function(){
        const v=kf.value.trim();if(!v)return;
        this.disabled=true;
        try{const r=await apiKald({a:"fskommentar",u:window.BPA_USER,oid:fsAaben,txt:v});
            if(r&&r.ok){kf.value="";fsVisOpslag();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}
        catch(e){this.disabled=false;}
    };
    if(kf)kf.addEventListener("keydown",e=>{if(e.key==="Enter"&&ks)ks.click();});
}

/* ===================== 🔍 Søgning på tværs af universet ===================== */
function soegLokalt(q){
    const hits={faq:[],form:[]};
    document.querySelectorAll("#v-faq details").forEach((d,i)=>{
        const t=(d.textContent||"").toLowerCase();
        if(t.indexOf(q)>=0&&hits.faq.length<5){
            const sum=d.querySelector("summary");
            hits.faq.push({titel:(sum?sum.textContent:"").trim(),i:i});
        }
    });
    document.querySelectorAll("#v-formularer .f-kort").forEach(k=>{
        const t=(k.textContent||"").toLowerCase();
        if(t.indexOf(q)>=0&&hits.form.length<5){
            const b=k.querySelector("b");
            hits.form.push({titel:(b?b.textContent:"").trim()});
        }
    });
    return hits;
}
async function koerSoegning(){
    const felt=document.getElementById("soegFelt");
    const q=(felt.value||"").trim().toLowerCase();
    if(q.length<2){felt.focus();return;}
    let ov=document.getElementById("soegOverlay");
    if(!ov){ov=document.createElement("div");ov.id="soegOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:994;background:rgba(30,55,76,.45);display:flex;align-items:flex-start;justify-content:center;padding:60px 18px 18px;overflow-y:auto";
        ov.addEventListener("click",e=>{if(e.target===ov)ov.remove();});
        document.body.appendChild(ov);}
    ov.innerHTML='<div id="soegKort" style="width:min(560px,100%);background:#fff;border-radius:22px;padding:24px;font-family:var(--font);box-shadow:0 30px 80px rgba(0,0,0,.4)"><p style="color:var(--graa)">🔍 Søger …</p></div>';
    const lokal=soegLokalt(q);
    let server={viden:[],nyheder:[]};
    try{const j=await apiKald({a:"soeg",q:q,u:window.BPA_USER||""});if(j&&j.ok)server=j;}catch(e){}
    const k=document.getElementById("soegKort");if(!k)return;
    const gruppe=(titel,rows)=>rows.length?'<div style="margin-top:14px"><b style="font-size:.85rem;color:var(--graa);letter-spacing:.05em">'+titel+'</b>'+rows.join("")+'</div>':'';
    const raekke=(html,attrs)=>'<div '+(attrs||"")+' style="padding:9px 12px;margin-top:5px;background:var(--flade);border-radius:11px;cursor:pointer;font-size:.9rem">'+html+'</div>';
    const antal=lokal.faq.length+lokal.form.length+(server.viden||[]).length+(server.nyheder||[]).length;
    k.innerHTML='<div style="display:flex;align-items:center;gap:10px;margin-bottom:4px"><b style="font-size:1.05rem">🔍 «'+fbaEsc(q)+'»</b>'+
        '<span style="color:var(--graa);font-size:.85rem">'+antal+' resultater</span>'+
        '<button id="soegLuk" style="margin-left:auto;border:0;background:rgba(30,55,76,.08);border-radius:10px;width:32px;height:32px;cursor:pointer">✕</button></div>'+
        (antal?'':'<p style="color:var(--graa)">Ingen resultater — prøv et andet ord, eller spørg chatten 🤖</p>')+
        gruppe("❓ SPØRGSMÅL & SVAR",lokal.faq.map(h=>raekke('<b>'+fbaEsc(h.titel)+'</b>','class="sg-faq" data-i="'+h.i+'"')))+
        gruppe("📖 FRA VIDENSBANKEN",(server.viden||[]).map(h=>raekke('<b>'+fbaEsc(h.titel)+'</b><div style="color:var(--graa);font-size:.83rem;margin-top:2px">'+fbaEsc(h.uddrag)+'</div><div style="color:var(--blaa);font-weight:700;font-size:.8rem;margin-top:4px">Spørg chatten om dette →</div>','class="sg-viden"')))+
        gruppe("📝 FORMULARER & SKABELONER",lokal.form.map(h=>raekke('<b>'+fbaEsc(h.titel)+'</b>','class="sg-form"')))+
        gruppe("📰 NYHEDER",(server.nyheder||[]).map(h=>raekke('<b>'+fbaEsc(h.titel)+'</b><div style="color:var(--graa);font-size:.83rem;margin-top:2px">'+fbaEsc(h.uddrag)+'</div>','class="sg-nyhed"')));
    document.getElementById("soegLuk").onclick=()=>ov.remove();
    k.querySelectorAll(".sg-faq").forEach(r=>{r.onclick=()=>{
        const i=parseInt(r.getAttribute("data-i"),10);
        ov.remove();location.hash="#/faq";
        setTimeout(()=>{const d=document.querySelectorAll("#v-faq details")[i];if(d){d.open=true;d.scrollIntoView({behavior:"smooth",block:"center"});}},200);
    };});
    k.querySelectorAll(".sg-form").forEach(r=>{r.onclick=()=>{ov.remove();location.hash="#/formularer";};});
    k.querySelectorAll(".sg-nyhed").forEach(r=>{r.onclick=()=>{ov.remove();location.hash="#/nyheder";};});
    k.querySelectorAll(".sg-viden").forEach(r=>{r.onclick=()=>{
        ov.remove();
        const fab=document.getElementById("chatFab"),win=document.getElementById("chatWin"),ci=document.getElementById("cInput");
        if(win&&!win.classList.contains("vis")&&fab)fab.click();
        if(ci){ci.value=q;ci.focus();}
    };});
}
(function(){
    const felt=document.getElementById("soegFelt"),knap=document.getElementById("soegKnap");
    if(!felt||!knap)return;
    knap.onclick=koerSoegning;
    felt.addEventListener("keydown",e=>{if(e.key==="Enter")koerSoegning();});
})();

/* ===================== 💙 Tilfredshedsmåling (borger, helt anonym) ===================== */
async function hentMaaling(){
    const u=window.BPA_USER||"";
    if(!u||u==="faelles")return;
    let j=null;try{j=await apiKald({a:"maalingspm",u:u});}catch(e){return;}
    if(!(j&&j.ok)||!j.aktiv||j.svaret)return;
    /* «Senere» udsætter KUN den aktuelle måling (mid) — en ny måling vises igen */
    try{const s=(localStorage.getItem("maaling_senere")||"").split("|");if(s[0]===(j.mid||"")&&parseInt(s[1]||"0",10)>Date.now())return;}catch(e){}
    let b=document.createElement("div");
    b.id="maalingBanner";
    b.style.cssText="max-width:1136px;width:calc(100% - 44px);margin:14px auto 0;box-sizing:border-box;border-radius:14px;background:linear-gradient(90deg,rgba(60,180,229,.12),rgba(231,0,76,.08));border:1px solid rgba(60,180,229,.3);padding:9px 18px;display:flex;gap:12px;align-items:center;font-size:.9rem;font-family:var(--font);flex-wrap:wrap";
    b.innerHTML='<span>💙</span><span><b>Vil du hjælpe os med at blive bedre?</b> Helt anonymt — det tager 2 minutter.</span>'+
        '<button id="maalStart" style="border:0;background:linear-gradient(120deg,var(--blaa),#1f8fc4);color:#fff;border-radius:999px;padding:7px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">Svar nu</button>'+
        '<button id="maalSenere" style="margin-left:auto;border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.83rem">Senere ✕</button>';
    const hb=document.getElementById("hjulBanner")||document.getElementById("nytBanner");
    if(hb&&hb.parentNode)hb.parentNode.insertBefore(b,hb.nextSibling);
    else{const m=document.querySelector("main");if(m&&m.parentNode)m.parentNode.insertBefore(b,m);}
    document.getElementById("maalSenere").onclick=()=>{
        try{localStorage.setItem("maaling_senere",(j.mid||"")+"|"+String(Date.now()+30*86400000));}catch(e){}
        b.remove();
    };
    document.getElementById("maalStart").onclick=()=>aabnMaaling(j.spm,b);
}
function aabnMaaling(spm,banner){
    let ov=document.getElementById("maalOverlay");
    if(!ov){ov=document.createElement("div");ov.id="maalOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:995;background:rgba(30,55,76,.45);display:flex;align-items:center;justify-content:center;padding:18px;overflow-y:auto";
        document.body.appendChild(ov);}
    const SMIL=["😞","😕","😐","🙂","😍"];
    ov.innerHTML='<div style="width:min(480px,100%);background:#fff;border-radius:22px;padding:26px;font-family:var(--font);box-shadow:0 30px 80px rgba(0,0,0,.4)">'+
        '<b style="font-size:1.1rem">💙 Hjælp os med at blive bedre</b>'+
        '<p style="color:var(--graa);font-size:.85rem;margin:4px 0 14px">Dine svar er helt anonyme — vi kan ikke se, hvem der har svaret hvad.</p>'+
        spm.map(s=>'<div style="margin-bottom:16px"><b style="font-size:.93rem">'+fbaEsc(s.t)+'</b>'+
            (s.type==="skala"
                ?'<div style="display:flex;gap:8px;margin-top:8px">'+SMIL.map((sm,i)=>'<button type="button" class="ma-skala" data-id="'+s.id+'" data-v="'+(i+1)+'" style="flex:1;border:2px solid #dbe6ee;background:#fff;border-radius:12px;padding:9px 0;cursor:pointer;font-size:1.3rem">'+sm+'</button>').join("")+'</div>'
                :'<textarea class="ma-tekst" data-id="'+s.id+'" rows="3" maxlength="1500" placeholder="Skriv frit …" style="width:100%;box-sizing:border-box;margin-top:8px;border:2px solid #dbe6ee;border-radius:12px;padding:10px 12px;font-family:var(--font);font-size:.9rem;outline:none"></textarea>')+
            '</div>').join("")+
        '<div style="display:flex;gap:10px"><button id="maalSend" class="fba-send" style="flex:1;border:0;border-radius:12px;padding:12px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.95rem">Send svar</button>'+
        '<button id="maalLuk" style="border:0;background:rgba(30,55,76,.08);border-radius:12px;padding:12px 18px;cursor:pointer;font-family:var(--font);color:var(--moerk)">Luk</button></div></div>';
    const valgte={};
    ov.querySelectorAll(".ma-skala").forEach(b=>{
        b.onclick=()=>{
            valgte[b.getAttribute("data-id")]=parseInt(b.getAttribute("data-v"),10);
            ov.querySelectorAll('.ma-skala[data-id="'+b.getAttribute("data-id")+'"]').forEach(x=>{x.style.borderColor=x===b?"var(--blaa)":"#dbe6ee";x.style.background=x===b?"rgba(60,180,229,.14)":"#fff";});
        };
    });
    document.getElementById("maalLuk").onclick=()=>ov.remove();
    document.getElementById("maalSend").onclick=async function(){
        ov.querySelectorAll(".ma-tekst").forEach(t=>{valgte[t.getAttribute("data-id")]=t.value.trim();});
        const mangler=spm.some(s=>s.type==="skala"&&!valgte[s.id]);
        if(mangler){alert("Vælg venligst en smiley ved alle spørgsmål.");return;}
        this.disabled=true;
        try{
            const r=await apiKald({a:"maalingsvar",u:window.BPA_USER||"",svar:valgte});
            if(r&&r.ok){
                ov.firstChild.innerHTML='<div style="text-align:center;padding:20px"><div style="font-size:2.5rem">💙</div><b style="font-size:1.1rem">Tusind tak!</b><p style="color:var(--graa)">Dine svar hjælper os med at gøre BPA-hverdagen bedre.</p></div>';
                if(banner)banner.remove();
                setTimeout(()=>ov.remove(),2500);
            }else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende — prøv igen.");}
        }catch(e){this.disabled=false;alert("Kunne ikke sende — tjek forbindelsen.");}
    };
}
setTimeout(hentMaaling,2500);

/* ===================== 🗓 Årshjul-SIDEN (kalender + årsoversigt) ===================== */
const AH_MDR=["januar","februar","marts","april","maj","juni","juli","august","september","oktober","november","december"];
var ahData=null,ahOffset=0,ahValgtDag=null;   /* var: routeren kan kalde synkront ved direkte load */
async function renderAarshjulSide(){
    const wrap=document.getElementById("ahWrap");if(!wrap)return;
    const u=window.BPA_USER||"";
    if(!u||u==="faelles"){wrap.innerHTML='<p style="color:var(--graa)">Log ind for at se dit årshjul.</p>';return;}
    if(!ahData){
        try{const j=await apiKald({a:"aarshjul",u:u,alle:1});if(j&&j.ok)ahData=j.punkter||[];}catch(e){}
        if(!ahData){wrap.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — prøv igen om lidt.</p>';return;}
    }
    ahRender();
}
function ahNøgle(d){return d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate();}
function ahRender(){
    const wrap=document.getElementById("ahWrap");if(!wrap)return;
    const punkter=ahData||[];
    /* begivenheder pr. dag */
    const prDag={};
    punkter.forEach(p=>{const k=ahNøgle(new Date(p.ts*1000));(prDag[k]=prDag[k]||[]).push(p);});
    /* 1) de nærmeste — fremhævet */
    const naeste=punkter.slice(0,3);
    let html=naeste.length?'<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px;margin-bottom:22px">'+
        naeste.map(p=>'<div style="background:#fff;border-radius:16px;padding:16px 18px;box-shadow:var(--skygge);border-left:5px solid '+(p.type==="borger"?"var(--roed)":"var(--blaa)")+'">'+
            '<div style="font-size:.78rem;font-weight:800;color:'+(p.type==="borger"?"var(--roed)":"var(--blaa)")+'">'+
            (p.dage===0?"I DAG":p.dage===1?"I MORGEN":"OM "+p.dage+" DAGE")+(p.type==="borger"?" · KUN DIG":"")+'</div>'+
            '<b style="display:block;margin:4px 0">'+fbaEsc(p.titel)+'</b>'+
            '<span style="color:var(--graa);font-size:.85rem">'+hjulDatoKort(p.ts)+'</span>'+
            (p.tekst?'<div style="color:#333;font-size:.85rem;margin-top:5px">'+fbaEsc(p.tekst)+'</div>':'')+'</div>').join("")+'</div>'
        :'<p style="color:var(--graa)">Ingen kommende datoer i dit årshjul endnu.</p>';
    /* 2) månedskalender */
    const idag=new Date();idag.setHours(0,0,0,0);
    const visM=new Date(idag.getFullYear(),idag.getMonth()+ahOffset,1);
    const foersteUgedag=(visM.getDay()+6)%7;   /* mandag=0 */
    const dageIMdr=new Date(visM.getFullYear(),visM.getMonth()+1,0).getDate();
    let celler='';
    for(let i=0;i<foersteUgedag;i++)celler+='<div></div>';
    for(let d=1;d<=dageIMdr;d++){
        const dato=new Date(visM.getFullYear(),visM.getMonth(),d);
        const k=ahNøgle(dato);
        const ev=prDag[k]||[];
        const erIdag=dato.getTime()===idag.getTime();
        const valgt=ahValgtDag===k;
        const harEgen=ev.some(p=>p.type==="borger");
        const farve=harEgen?"var(--roed)":"var(--blaa)";
        /* dage MED indhold: farvet baggrund + kant + titlen direkte i cellen — ikke til at overse */
        const bg=ev.length?(harEgen?"rgba(231,0,76,.10)":"rgba(60,180,229,.13)"):"#fff";
        const kant=erIdag?"2px solid var(--blaa)":(ev.length?"2px solid "+farve:"1px solid rgba(30,55,76,.07)");
        celler+='<div class="ah-dag" data-k="'+k+'" style="min-height:64px;border-radius:11px;padding:5px 6px;font-size:.82rem;background:'+bg+';border:'+kant+';'+
            (valgt?'outline:3px solid var(--groen);':'')+
            (ev.length?'cursor:pointer;':'')+'">'+
            '<b style="'+(erIdag?'color:var(--blaa);':'')+(ev.length?'color:'+farve+';':'')+'">'+d+'</b>'+
            (ev.length?'<div style="margin-top:3px;font-size:.6rem;font-weight:800;color:'+farve+';line-height:1.25;overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">'+
                fbaEsc(ev[0].titel)+(ev.length>1?' +'+(ev.length-1):'')+'</div>':'')+
            '</div>';
    }
    const valgtEv=ahValgtDag?(prDag[ahValgtDag]||[]):[];
    html+='<div class="fba-kort" style="background:var(--flade)">'+
        '<div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">'+
        '<button id="ahForrige" '+(ahOffset<=0?'disabled':'')+' style="border:0;background:#fff;border-radius:10px;width:34px;height:34px;cursor:pointer;box-shadow:var(--skygge)">‹</button>'+
        '<b style="flex:1;text-align:center;font-size:1.05rem;text-transform:capitalize">'+AH_MDR[visM.getMonth()]+' '+visM.getFullYear()+'</b>'+
        '<button id="ahNaeste" '+(ahOffset>=11?'disabled':'')+' style="border:0;background:#fff;border-radius:10px;width:34px;height:34px;cursor:pointer;box-shadow:var(--skygge)">›</button></div>'+
        '<div style="display:grid;grid-template-columns:repeat(7,1fr);gap:5px;font-size:.72rem;font-weight:800;color:var(--graa);text-align:center;margin-bottom:4px">'+
        ["MA","TI","ON","TO","FR","LØ","SØ"].map(x=>'<div>'+x+'</div>').join("")+'</div>'+
        '<div style="display:grid;grid-template-columns:repeat(7,1fr);gap:5px">'+celler+'</div>'+
        '<div style="display:flex;gap:14px;margin-top:10px;font-size:.78rem;color:var(--graa);flex-wrap:wrap">'+
        '<span><span style="display:inline-block;width:14px;height:14px;border-radius:4px;background:rgba(60,180,229,.13);border:2px solid var(--blaa);vertical-align:-2px"></span> fælles for alle</span>'+
        '<span><span style="display:inline-block;width:14px;height:14px;border-radius:4px;background:rgba(231,0,76,.10);border:2px solid var(--roed);vertical-align:-2px"></span> kun dig</span>'+
        '<span>· tryk på en dag for detaljer</span></div>'+
        (valgtEv.length?'<div style="margin-top:10px;background:#fff;border-radius:12px;padding:12px 14px;box-shadow:var(--skygge)">'+
            valgtEv.map(p=>'<div style="margin:4px 0"><b>'+fbaEsc(p.titel)+'</b>'+(p.type==="borger"?' <span style="color:var(--roed);font-size:.78rem;font-weight:800">kun dig</span>':'')+
                (p.tekst?'<div style="color:#333;font-size:.85rem">'+fbaEsc(p.tekst)+'</div>':'')+'</div>').join("")+'</div>':'')+
        '</div>';
    /* 3) året måned for måned */
    const prMdr={};
    punkter.forEach(p=>{const d=new Date(p.ts*1000);const k=d.getFullYear()+"-"+d.getMonth();(prMdr[k]=prMdr[k]||{navn:AH_MDR[d.getMonth()]+" "+d.getFullYear(),liste:[]}).liste.push(p);});
    html+='<h3 style="margin:26px 0 10px">Året måned for måned</h3>'+
        Object.keys(prMdr).map(k=>{
            const m=prMdr[k];
            return '<div class="fba-kort"><b style="text-transform:capitalize">'+m.navn+'</b>'+
                m.liste.map(p=>'<div style="display:flex;gap:10px;padding:7px 0;border-bottom:1px solid rgba(30,55,76,.06);font-size:.9rem;align-items:baseline">'+
                    '<b style="min-width:44px;color:'+(p.type==="borger"?"var(--roed)":"var(--blaa)")+'">'+new Date(p.ts*1000).getDate()+'.</b>'+
                    '<div><b>'+fbaEsc(p.titel)+'</b>'+(p.type==="borger"?' <span style="color:var(--roed);font-size:.75rem;font-weight:800">kun dig</span>':'')+
                    (p.gentag==="maaned"?' <span style="color:var(--graa);font-size:.75rem">· hver måned</span>':p.gentag==="aar"?' <span style="color:var(--graa);font-size:.75rem">· hvert år</span>':'')+
                    (p.tekst?'<div style="color:var(--graa);font-size:.83rem">'+fbaEsc(p.tekst)+'</div>':'')+'</div></div>').join("")+'</div>';
        }).join("");
    wrap.innerHTML=html;
    /* lyttere */
    const fk=document.getElementById("ahForrige"),nk=document.getElementById("ahNaeste");
    if(fk)fk.onclick=()=>{if(ahOffset>0){ahOffset--;ahValgtDag=null;ahRender();}};
    if(nk)nk.onclick=()=>{if(ahOffset<11){ahOffset++;ahValgtDag=null;ahRender();}};
    wrap.querySelectorAll(".ah-dag").forEach(c=>{
        const k=c.getAttribute("data-k");
        if((prDag[k]||[]).length)c.onclick=()=>{ahValgtDag=ahValgtDag===k?null:k;ahRender();};
    });
}

/* ===================== 📋 Standardsvar: «/» i svarfelter + vedligehold ===================== */
var svarData=null,svarBoksEl=null;   /* var: kaldes synkront fra router/skelet — undgå TDZ */
async function svarHent(){
    if(svarData)return svarData;
    try{const j=await apiKald({a:"svarliste",token:fbaSess().token});if(j&&j.ok)svarData=j;}catch(e){}
    return svarData;
}
function svarAlle(){return svarData?(svarData.faelles||[]).concat(svarData.mine||[]):[];}
function svarSvæver(){   /* fælles svæve-boks til forslagene (én for hele siden) */
    if(!svarBoksEl){
        svarBoksEl=document.createElement("div");
        svarBoksEl.style.cssText="position:fixed;z-index:998;display:none;flex-wrap:wrap;gap:6px;background:#fff;border-radius:12px;box-shadow:0 18px 44px rgba(30,55,76,.3);padding:8px;max-width:440px";
        document.body.appendChild(svarBoksEl);
    }
    return svarBoksEl;
}
function svarForslagOpsæt(felt,navnFn){
    if(!felt||felt._svarSat)return;felt._svarSat=1;
    const boks=svarSvæver();
    function luk(){if(boks._felt===felt){boks.style.display="none";boks._felt=null;}}
    felt.addEventListener("input",async ()=>{
        const m=/^\/(.*)$/.exec(felt.value);
        if(!m){luk();return;}
        await svarHent();
        const q=m[1].toLowerCase();
        const hits=svarAlle().filter(s=>!q||s.titel.toLowerCase().indexOf(q)>=0).slice(0,8);
        if(!hits.length){luk();return;}
        boks.innerHTML=hits.map((s,i)=>'<button type="button" data-i="'+i+'" style="border:1px solid rgba(60,180,229,.4);background:#fff;color:var(--moerk);border-radius:999px;padding:6px 12px;cursor:pointer;font-family:var(--font);font-size:.8rem;font-weight:700">'+(s.faelles?"⭐ ":"")+fbaEsc(s.titel)+'</button>').join("");
        boks.style.display="flex";boks._felt=felt;
        const r=felt.getBoundingClientRect();
        boks.style.left=Math.max(8,r.left)+"px";
        boks.style.top=Math.max(8,r.top-boks.offsetHeight-8)+"px";
        boks.querySelectorAll("button").forEach(b=>{
            b.onclick=()=>{
                const s=hits[parseInt(b.getAttribute("data-i"),10)];
                felt.value=s.txt.replace(/\{navn\}/g,(navnFn&&navnFn())||"");
                luk();felt.focus();
            };
        });
    });
    felt.addEventListener("blur",()=>setTimeout(luk,250));
}
async function renderSvarPanel(){
    const boks=document.getElementById("svarBoksPanel");if(!boks)return;
    svarData=null;await svarHent();
    if(!svarData){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    const ejer=!!svarData.ejer;
    const raekke=(s,kanRet)=>'<div class="fba-top" style="margin-top:6px;padding:8px 12px;background:#fff;border-radius:10px;border:1px solid rgba(30,55,76,.07)">'+
        '<b>'+(s.faelles?'⭐ ':'')+fbaEsc(s.titel)+'</b>'+
        '<span style="color:var(--graa);font-size:.8rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:44ch">'+fbaEsc(s.txt)+'</span>'+
        (kanRet?'<button class="fba-done sv-ret" data-id="'+s.id+'" style="margin-left:auto">✏️</button><button class="fba-done sv-slet" data-id="'+s.id+'">🗑</button>':'')+'</div>';
    boks.innerHTML='<p style="color:var(--graa);font-size:.83rem">Skriv <b>/</b> i et svarfelt (live-chat eller feedback) og vælg — <b>{navn}</b> bliver automatisk til borgerens fornavn.</p>'+
        '<b>⭐ Fælles'+(ejer?'':' <span style="color:var(--graa);font-weight:400;font-size:.8rem">(kun ejeren redigerer)</span>')+'</b>'+
        ((svarData.faelles||[]).map(s=>raekke(s,ejer)).join("")||'<p style="color:var(--graa);font-size:.85rem;margin:4px 0">Ingen endnu.</p>')+
        '<b style="display:block;margin-top:12px">🙋 Mine egne</b>'+
        ((svarData.mine||[]).map(s=>raekke(s,true)).join("")||'<p style="color:var(--graa);font-size:.85rem;margin:4px 0">Ingen endnu.</p>')+
        '<div style="display:grid;gap:8px;margin-top:12px;background:#fff;border-radius:12px;padding:12px;border:1px dashed rgba(30,55,76,.2)" id="svOpret">'+
        '<b id="svOverskrift">➕ Nyt standardsvar</b>'+
        '<input id="svTitel" maxlength="60" placeholder="Titel (den du søger på med /)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font)">'+
        '<textarea id="svTxt" rows="3" maxlength="2000" placeholder="Selve svaret … brug {navn} for borgerens fornavn" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font)"></textarea>'+
        (ejer?'<label style="font-size:.85rem"><input type="checkbox" id="svFaelles"> ⭐ Fælles — alle admins kan bruge det</label>':'')+
        '<div style="display:flex;gap:8px"><button id="svGem" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">💾 Gem</button>'+
        '<button id="svNulstil" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.83rem" hidden>Annullér redigering</button></div></div>';
    let redigId="";
    const alle=svarAlle();
    boks.querySelectorAll(".sv-ret").forEach(b=>{b.onclick=()=>{
        const s=alle.find(x=>x.id===b.getAttribute("data-id"));if(!s)return;
        redigId=s.id;
        document.getElementById("svTitel").value=s.titel;
        document.getElementById("svTxt").value=s.txt;
        const f=document.getElementById("svFaelles");if(f)f.checked=!!s.faelles;
        document.getElementById("svOverskrift").textContent="✏️ Redigér: "+s.titel;
        document.getElementById("svNulstil").hidden=false;
    };});
    boks.querySelectorAll(".sv-slet").forEach(b=>{b.onclick=async ()=>{
        if(!confirm("Slet standardsvaret?"))return;
        try{const r=await apiKald({a:"svarslet",token:fbaSess().token,id:b.getAttribute("data-id")});
            if(r&&r.ok)renderSvarPanel();else alert((r&&r.fejl)||"Kunne ikke slette");}catch(e){}
    };});
    document.getElementById("svNulstil").onclick=()=>renderSvarPanel();
    document.getElementById("svGem").onclick=async function(){
        const titel=document.getElementById("svTitel").value.trim(),txt=document.getElementById("svTxt").value.trim();
        if(!titel||!txt){alert("Udfyld både titel og tekst.");return;}
        const f=document.getElementById("svFaelles");
        this.disabled=true;
        try{const r=await apiKald({a:"svargem",token:fbaSess().token,id:redigId,titel:titel,txt:txt,faelles:f&&f.checked?1:0});
            if(r&&r.ok)renderSvarPanel();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
}

/* ===================== 👤 Borger-profilkort (klik på et borgernavn) ===================== */
document.addEventListener("click",e=>{
    const el=e.target.closest&&e.target.closest("[data-bku]");
    if(el&&fbaSess()){e.preventDefault();e.stopPropagation();aabnBorgerKort(el.getAttribute("data-bku"));}
});
/* ===== Konsulentens tilknyttede borgere — søgbar oversigt (åbnes fra 360-kortet) ===== */
async function aabnKonsBorgere(konsNavn,antal){
    var ov=document.getElementById("kbOverlay");
    if(!ov){ov=document.createElement("div");ov.id="kbOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:998;background:rgba(30,55,76,.5);display:flex;align-items:center;justify-content:center;padding:18px";
        ov.addEventListener("click",e=>{if(e.target===ov)ov.remove();});
        document.body.appendChild(ov);}
    ov.innerHTML='<div style="width:min(520px,100%);max-height:86vh;display:flex;flex-direction:column;background:#fff;border-radius:22px;box-shadow:0 30px 80px rgba(0,0,0,.4);overflow:hidden;font-family:var(--font)">'+
        '<div style="padding:20px 24px 12px;display:flex;align-items:center;gap:11px">'+
        '<span style="font-size:1.5rem;flex:none">🧑‍🦽</span>'+
        '<div style="flex:1;min-width:0"><b style="font-size:1.12rem">Tilknyttede borgere</b>'+
        '<div style="color:var(--graa);font-size:.82rem">'+fbaEsc(konsNavn||"")+' · <span id="kbTael">'+(antal||0)+'</span> borgere</div></div>'+
        '<button id="kbLuk" style="border:0;background:rgba(30,55,76,.08);border-radius:10px;width:32px;height:32px;cursor:pointer;font-size:.95rem;flex:none">✕</button>'+
        '</div>'+
        '<div style="padding:0 24px 12px"><input id="kbSoeg" placeholder="🔎 Søg navn, kundenr, tlf eller email …" style="width:100%;box-sizing:border-box;border:1.5px solid rgba(30,55,76,.18);border-radius:12px;padding:11px 14px;font-family:var(--font);font-size:.95rem"></div>'+
        '<div id="kbListe" style="flex:1;overflow-y:auto;padding:2px 14px 16px"><p style="color:var(--graa);padding:12px 10px">⏳ Henter …</p></div>'+
        '</div>';
    document.getElementById("kbLuk").onclick=()=>ov.remove();
    var sess=fbaSess(),j=null;
    try{j=await apiKald({a:"konsborgere",token:sess.token,kons:konsNavn});}catch(e){}
    var liste=(j&&j.ok&&j.borgere)?j.borgere:[];
    var kbListe=document.getElementById("kbListe");
    function rowHTML(b){
        var av=b.foto
            ?'<span style="position:relative;width:38px;height:38px;border-radius:50%;overflow:hidden;flex:none;background:#eef4f8"><img class="hf-av-img" src="mithf-api.php?a=pfoto&u='+encodeURIComponent(String(b.u).toLowerCase())+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0"></span>'
            :'<span style="width:38px;height:38px;border-radius:50%;flex:none;background:rgba(60,180,229,.15);display:grid;place-items:center;font-size:1rem">🧑‍🦽</span>';
        return '<div class="kb-r" data-u="'+fbaEsc(b.u)+'" style="display:flex;gap:11px;align-items:center;padding:9px 10px;border-radius:12px;cursor:pointer;transition:background .12s">'+
            av+'<div style="flex:1;min-width:0"><b style="font-size:.92rem;display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+fbaEsc(b.navn)+'</b>'+
            '<div style="color:var(--graa);font-size:.78rem">'+fbaEsc(b.u)+'</div></div>'+
            '<span style="color:var(--blaa);font-size:1.1rem;flex:none">›</span></div>';
    }
    function tegn(q){
        q=(q||"").toLowerCase().trim();
        var vis=q?liste.filter(function(b){return (b.navn+" "+b.u+" "+(b.mobil||"")+" "+(b.email||"")).toLowerCase().indexOf(q)>=0;}):liste;
        var tael=document.getElementById("kbTael");if(tael)tael.textContent=vis.length;
        kbListe.innerHTML=vis.length?vis.map(rowHTML).join("")
            :'<p style="color:var(--graa);padding:20px 10px;text-align:center">Ingen borgere matcher «'+fbaEsc(q)+'»</p>';
        if(typeof hfAvatarWire==="function")hfAvatarWire(kbListe);
        kbListe.querySelectorAll(".kb-r").forEach(function(r){
            r.onclick=function(){ov.remove();aabnBorgerKort(r.getAttribute("data-u"));};
            r.onmouseenter=function(){r.style.background="#eaf4fb";};
            r.onmouseleave=function(){r.style.background="";};
        });
    }
    if(!liste.length)kbListe.innerHTML='<p style="color:var(--graa);padding:20px 10px;text-align:center">Ingen tilknyttede borgere fundet.</p>';
    else tegn("");
    var soeg=document.getElementById("kbSoeg");
    if(soeg){soeg.oninput=function(){tegn(soeg.value);};setTimeout(function(){try{soeg.focus();}catch(e){}},90);}
}
/* 👤 mini-stamkort for kontaktperson/nærtstående — popup med sep-toggle, kontaktdata + redigerbar note */
function aabnKpKort(u,kp,borgerNavn){
    kp=kp||{};
    var gl=document.getElementById("kpOverlay");if(gl)gl.remove();
    var ov=document.createElement("div");ov.id="kpOverlay";
    ov.style.cssText="position:fixed;inset:0;background:rgba(20,35,50,.55);z-index:9998;display:flex;align-items:center;justify-content:center;padding:16px";
    var esc=function(s){return fbaEsc(String(s==null?"":s));};
    var felt=function(id,lab,val,ph){return '<label style="display:block;margin-top:10px;font-size:.82rem;color:var(--graa);font-weight:700">'+lab+'<input id="'+id+'" value="'+esc(val)+'" placeholder="'+esc(ph||"")+'" style="display:block;width:100%;box-sizing:border-box;margin-top:3px;border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:9px 12px;font-family:var(--font);font-size:.9rem"></label>';};
    ov.innerHTML='<div style="background:#fff;border-radius:16px;max-width:420px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,.3);overflow:hidden">'+
        '<div style="padding:16px 20px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;display:flex;align-items:center;gap:10px"><span style="font-size:1.3rem">👤</span><div style="flex:1"><b style="font-size:1.05rem">Kontaktperson</b><div style="opacity:.8;font-size:.78rem">'+esc(borgerNavn)+'</div></div><button id="kpLuk" style="border:0;background:rgba(255,255,255,.18);color:#fff;border-radius:9px;width:30px;height:30px;cursor:pointer;font-size:.9rem">✕</button></div>'+
        '<div style="padding:16px 20px">'+
        '<label style="display:flex;gap:9px;align-items:flex-start;cursor:pointer;font-size:.9rem"><input type="checkbox" id="kpSep" '+(kp.sep?"checked":"")+' style="margin-top:3px"><span>En <b>nærtstående</b> logger ind i mitHF på vegne af borgeren<br><span style="color:var(--graa);font-size:.78rem">Vises som en note på borgerens kort. Borgerens eget navn beholdes.</span></span></label>'+
        felt("kpNavn","Kontaktpersonens navn",kp.navn,"fx Anne (datter)")+
        felt("kpEmail","Email (valgfri)",kp.email,"kontaktperson@eksempel.dk")+
        felt("kpTlf","Telefon (valgfri)",kp.tlf,"12 34 56 78")+
        '<label style="display:block;margin-top:10px;font-size:.82rem;color:var(--graa);font-weight:700">Note på kortet (valgfri — tom = standardtekst)<textarea id="kpNote" rows="2" placeholder="Logges ind i mitHF af … på vegne af borgeren." style="display:block;width:100%;box-sizing:border-box;margin-top:3px;border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:9px 12px;font-family:var(--font);font-size:.9rem;resize:vertical">'+esc(kp.note)+'</textarea></label>'+
        '<div style="display:flex;gap:8px;margin-top:16px"><button id="kpGem" class="fba-send" style="flex:1;border:0;border-radius:10px;padding:11px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.9rem">💾 Gem</button><button id="kpAnnul" style="border:1px solid rgba(30,55,76,.2);background:#fff;border-radius:10px;padding:11px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.9rem">Annullér</button></div>'+
        '<div id="kpFejl" style="color:var(--roed);font-size:.82rem;margin-top:8px"></div>'+
        '</div></div>';
    document.body.appendChild(ov);
    var luk=function(){ov.remove();};
    ov.onclick=function(e){if(e.target===ov)luk();};
    document.getElementById("kpLuk").onclick=luk;
    document.getElementById("kpAnnul").onclick=luk;
    document.getElementById("kpGem").onclick=async function(){
        var sess=fbaSess();if(!sess)return;
        this.disabled=true;
        try{var r=await apiKald({a:"bkpgem",token:sess.token,u:u,
            sep:document.getElementById("kpSep").checked?1:0,
            navn:document.getElementById("kpNavn").value.trim(),
            email:document.getElementById("kpEmail").value.trim(),
            tlf:document.getElementById("kpTlf").value.trim(),
            note:document.getElementById("kpNote").value.trim()});
            if(r&&r.ok){luk();aabnBorgerKort(u);}
            else{this.disabled=false;document.getElementById("kpFejl").textContent=(r&&r.fejl)||"Kunne ikke gemme";}}
        catch(e){this.disabled=false;document.getElementById("kpFejl").textContent="Kunne ikke gemme — tjek forbindelsen.";}
    };
}
async function aabnBorgerKort(u){
    let ov=document.getElementById("bkOverlay");
    if(!ov){
        ov=document.createElement("div");ov.id="bkOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:996;background:rgba(30,55,76,.45);display:flex;align-items:center;justify-content:center;padding:18px";
        ov.addEventListener("click",e=>{if(e.target===ov)ov.remove();});
        document.body.appendChild(ov);
    }
    ov.innerHTML='<div id="bkKort" style="width:min(540px,100%);max-height:88vh;overflow-y:auto;background:#fff;border-radius:22px;padding:26px;font-family:var(--font);box-shadow:0 30px 80px rgba(0,0,0,.4)"><p style="color:var(--graa)">⏳ Henter …</p></div>';
    try{
        const j = await apiKald({a:"borgerkort",token:fbaSess().token,u:u});
        const k=document.getElementById("bkKort");if(!k)return;
        if(!(j&&j.ok)){k.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
        const linje=(ik,t,v)=>'<div style="display:flex;gap:10px;padding:8px 0;border-bottom:1px solid rgba(30,55,76,.06);font-size:.9rem;align-items:center"><span>'+ik+'</span><span style="color:var(--graa)">'+t+'</span><b style="margin-left:auto;text-align:right">'+v+'</b></div>';
        const sektion=t=>'<div style="font-size:.7rem;font-weight:800;letter-spacing:.08em;text-transform:uppercase;color:var(--graa);margin:12px 0 2px">'+t+'</div>';
        k.innerHTML='<div style="display:flex;gap:12px;align-items:center;margin-bottom:12px">'+
            '<span style="position:relative;width:46px;height:46px;border-radius:50%;overflow:hidden;background:rgba(60,180,229,.15);display:grid;place-items:center;font-size:1.3rem;flex:none">👤<img class="hf-av-img" src="'+API_URL+'?a=pfoto&u='+encodeURIComponent(String(u).toLowerCase())+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0"></span>'+
            '<div><b style="font-size:1.05rem">'+fbaEsc(j.stamNavn||j.navn||j.u)+'</b>'+
            (j.retMithf?' <button id="bkNavn" title="Omdøb i adgangslisten" style="border:0;background:rgba(30,55,76,.08);border-radius:7px;cursor:pointer;font-size:.72rem;padding:2px 7px">✏️</button>':'')+
            '<div style="color:var(--graa);font-size:.8rem">'+fbaEsc(j.u)+
            (j.retMithf?' <button id="bkKode" title="Skift brugernavn (login-koden)" style="border:0;background:rgba(30,55,76,.08);border-radius:7px;cursor:pointer;font-size:.68rem;padding:1px 6px">✏️</button>':'')+
            (j.adgangLukket?' <span style="color:var(--roed);font-weight:800;font-size:.75rem">⛔ adgang lukket</span>':'')+
            '</div></div>'+
            '<button id="bkLuk" style="margin-left:auto;border:0;background:rgba(30,55,76,.08);border-radius:10px;width:32px;height:32px;cursor:pointer;font-size:.95rem">✕</button></div>'+
            (j.kp&&j.kp.sep?'<div style="display:flex;gap:8px;align-items:flex-start;background:rgba(60,180,229,.1);border:1px solid rgba(60,180,229,.28);border-radius:10px;padding:8px 11px;margin:0 0 8px;font-size:.85rem"><span>👤</span><span style="flex:1;color:var(--moerk)">'+fbaEsc(j.kp.note||("Logges ind i mitHF af "+(j.kp.navn||"en nærtstående")+" på vegne af borgeren."))+'</span>'+(j.retMithf?'<button id="bkKpRet" title="Redigér kontaktperson" style="border:0;background:rgba(30,55,76,.08);border-radius:7px;cursor:pointer;font-size:.72rem;padding:2px 7px;flex:none">✏️</button>':'')+'</div>':'')+
            linje("🤝","Konsulent",'<span id="bkKonsVis">'+fbaEsc(j.konsulent||"—")+'</span>'+
                (j.retKons?' <button id="bkKons" title="Skift konsulent" style="border:0;background:rgba(30,55,76,.08);border-radius:7px;cursor:pointer;font-size:.75rem;padding:2px 7px">✏️</button>':''))+
            (j.direkte?linje("☎️","Direkte nr",'<a href="tel:'+(String(j.direkte).length===8?"+45":"")+fbaEsc(j.direkte)+'" style="color:var(--blaa);font-weight:700;text-decoration:none">'+fbaEsc(String(j.direkte).length===8?String(j.direkte).replace(/(\d{2})(\d{2})(\d{2})(\d{2})/,"$1 $2 $3 $4"):String(j.direkte))+'</a>'):"")+
            (j.mobil?linje("📱","Mobil",'<a href="tel:'+(String(j.mobil).length===8?"+45":"")+fbaEsc(j.mobil)+'" style="color:var(--blaa);font-weight:700;text-decoration:none">'+fbaEsc(String(j.mobil).length===8?String(j.mobil).replace(/(\d{2})(\d{2})(\d{2})(\d{2})/,"$1 $2 $3 $4"):String(j.mobil))+'</a>'):"")+
            (j.email?linje("✉️","Email",'<a href="mailto:'+fbaEsc(j.email)+'" style="color:var(--blaa);font-weight:700;text-decoration:none;word-break:break-all">'+fbaEsc(j.email)+'</a>'):"")+
            (j.retMithf?linje("🩵","Kunde-id (mitHF)",j.mithfId?fbaEsc(j.mithfId):'<span style="color:var(--graa);font-weight:400">— ikke sat</span>'):"")+
            (j.retMithf?linje("🆔","Bruger-id (login)",j.brugereId?fbaEsc(j.brugereId):'<span style="color:var(--graa);font-weight:400">— ikke sat</span>'):"")+
            linje("🔑","Sidst logget ind",j.sidstLogin?fbaDato(j.sidstLogin):"aldrig")+
            linje("🎓","Læringshub",j.hubEmner+" emner gennemført")+
            linje("🤖","Chat-spørgsmål",String(j.chatSp))+
            linje("🌍","Fællesskabet",(j.fsOpslag||0)+" opslag")+
            (j.datoer&&j.datoer.length?sektion("🗓 Kommende datoer")+
                j.datoer.map(d=>linje("📅",fbaEsc(d.dato.split("-").reverse().join(".")),fbaEsc(d.titel))).join(""):"")+
            sektion("💬 Live-chats — "+j.chats+" i alt")+
            ((j.chatListe||[]).map(c=>linje(c.status==="aktiv"?"🟢":"⚪",fbaDato(c.ts),fbaEsc(c.adm?("hos "+c.adm):c.status))).join("")||'<div style="color:var(--graa);font-size:.82rem;padding:6px 0">ingen endnu</div>')+
            sektion("📬 Feedback — "+j.traade+" i alt · "+j.aabne+" åbne")+
            ((j.seneste||[]).map(s=>linje(s.aaben?"🔴":"✅",fbaDato(s.ts),'<span style="font-weight:400;font-size:.8rem">'+fbaEsc(s.txt)+'</span>')).join("")||'<div style="color:var(--graa);font-size:.82rem;padding:6px 0">ingen endnu</div>')+
            '<div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:14px">'+
            (j.retBorgerweb&&j.brugereId?'<button id="bkBorgerweb" title="Log ind SOM borgeren i mitHF (borgerweb) — fx for at hjælpe med vagtplanen. Åbner et rent vindue, præcis som borgeren selv ser det." style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--blaa),#2a91c4);border:0;color:#fff;font-weight:800">🔓 Log på borgerweb</button>':'')+
            (j.konsAntal>0?'<button id="bkTilknyt" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--groen),#7bbf00);border:0;color:#0d2a12;font-weight:800">🧑‍🦽 Tilknyttede borgere ('+j.konsAntal+')</button>':'')+
            '<button id="bkFeedback" style="'+PROG_KNAP+'">📬 Åbn borgerens feedback</button>'+
            '<button id="bkMithf" style="'+PROG_KNAP+(j.mithfId?';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0':'')+'">'+
            (j.mithfId?"↗ Åbn borgeren i mitHF":"↗ Åbn mitHF (intet direkte link)")+'</button>'+
            (j.retMithf?'<button id="bkMid" title="Borgerens kunde-id i mitHF (stamkort) — bruges til det direkte opslag" style="'+PROG_KNAP+'">✏️ kunde-id'+(j.mithfId?" ("+fbaEsc(j.mithfId)+")":"")+'</button>':'')+
            (j.retMithf?'<button id="bkBid" title="Kontaktpersonens brugere-id i mitHF — bruges til borgerens login (SSO)" style="'+PROG_KNAP+'">✏️ brugere-id'+(j.brugereId?" ("+fbaEsc(j.brugereId)+")":"")+'</button>':'')+
            (j.retMithf?'<button id="bkKp" title="Kontaktperson / nærtstående der logger ind på vegne af borgeren" style="'+PROG_KNAP+((j.kp&&j.kp.sep)?';background:rgba(60,180,229,.14);color:var(--moerk)':'')+'">'+((j.kp&&j.kp.sep)?'👤 Kontaktperson':'👤 + Kontaktperson')+'</button>':'')+
            (j.retMithf?'<button id="bkAdgang" style="'+PROG_KNAP+';color:'+(j.adgangLukket?'#41630a':'var(--roed)')+'">'+(j.adgangLukket?"✅ Genåbn adgang":"⛔ Luk adgang")+'</button>':'')+
            '</div>';
        if(typeof hfAvatarWire==="function")hfAvatarWire(k);   /* profilbillede på 360-avataren (fallback: 👤) */
        document.getElementById("bkLuk").onclick=()=>ov.remove();
        var bkBorgerwebBtn = document.getElementById("bkBorgerweb");
        if (bkBorgerwebBtn) bkBorgerwebBtn.onclick = () => aabnBorgerwebAdm(u, j.stamNavn || j.navn || j.u);

        var bkTilknytBtn = document.getElementById("bkTilknyt");
        if(bkTilknytBtn) bkTilknytBtn.onclick = () => aabnKonsBorgere(j.konsNavn||j.stamNavn||j.navn||u,j.konsAntal);
        document.getElementById("bkFeedback").onclick=()=>{
            ov.remove();
            fbaG="borger";
            if(location.hash.replace(/^#\/?/,"")==="feedback-admin")visOmraade("feedback");
            else{try{sessionStorage.setItem("omr_fokus","feedback");}catch(e){}location.hash="#/feedback-admin";}
        };
        document.getElementById("bkMithf").onclick=()=>{
            /* borgere → kunde-siden; internt personale → admin_brugere-profilen */
            const url=j.mithfId?(j.mithfType==="adm"
                    ?"https://handicapformidlingen.temponizer.dk/index.php?page=admin_brugere_show&admin_brugere_id="+encodeURIComponent(j.mithfId)
                    :"https://handicapformidlingen.temponizer.dk/index.php?page=showkunde&kunder_id="+encodeURIComponent(j.mithfId)+"#stamoplysninger")
                :"https://handicapformidlingen.temponizer.dk/";
            /* tving=true når vi har et direkte borger-link: mitHF-fanen skal NAVIGERES til borgeren,
         ikke bare fokuseres (samme side + samme login — intet går tabt) */
            if(window.HFVin&&window.HFVin.aabn)window.HFVin.aabn("hf-prog-mithf",url,"","mitHF",!!j.mithfId);
            else window.open(url,"_blank");
        };
        const bkKode=document.getElementById("bkKode");
        if(bkKode)bkKode.onclick=async()=>{
            const ny=(window.prompt("Nyt BRUGERNAVN (login-koden) for "+(j.navn||j.u)+":\n\nOBS: Kodeord, mitHF-id, konsulent og fællesskab følger med — men historik i logfiler beholder det gamle brugernavn.",j.u)||"").trim().toLowerCase();
            if(!ny||ny===j.u)return;
            if(!confirm("Skift brugernavn «"+j.u+"» → «"+ny+"»?\nBorgeren skal fremover logge ind med det nye."))return;
            try{const r=await apiKald({a:"borgerkode",token:fbaSess().token,u:u,ny:ny});
                if(r&&r.ok){fbaAdgang=null;fbaKons=null;aabnBorgerKort(r.ny);}
                else alert((r&&r.fejl)||"Kunne ikke skifte");}catch(e){}
        };
        const bkAdgang=document.getElementById("bkAdgang");
        if(bkAdgang)bkAdgang.onclick=async()=>{
            if(!confirm(j.adgangLukket?"Genåbn adgangen for "+(j.navn||j.u)+"?"
                :"Luk adgangen for "+(j.navn||j.u)+"?\nBorgeren kan ikke logge ind i universet, før den genåbnes."))return;
            try{const r=await apiKald({a:"borgeradgangtoggle",token:fbaSess().token,login:u,til:j.adgangLukket?1:0,navn:j.navn||""});
                if(r&&r.ok){fbaAdgang=null;aabnBorgerKort(u);}
                else alert((r&&r.fejl)||"Kunne ikke gemme");}catch(e){}
        };
        const bkNavn=document.getElementById("bkNavn");
        if(bkNavn)bkNavn.onclick=async()=>{
            const nyt=(window.prompt("Nyt navn for "+j.u+":",j.navn||"")||"").trim();
            if(!nyt||nyt===j.navn)return;
            try{const r=await apiKald({a:"borgernavn",token:fbaSess().token,u:u,navn:nyt});
                if(r&&r.ok){fbaAdgang=null;fbaKons=null;aabnBorgerKort(u);}
                else alert((r&&r.fejl)||"Kunne ikke omdøbe");}catch(e){}
        };
        const bkMid=document.getElementById("bkMid");
        if(bkMid)bkMid.onclick=async()=>{
            const mid=(window.prompt("Borgerens mitHF-id (tallet i mitHF-adressen, fx 2833).\nTomt felt fjerner det direkte link:",j.mithfId||"")||"").trim();
            if(mid===(j.mithfId||""))return;
            if(mid&&!/^\d{1,8}$/.test(mid)){alert("mitHF-id er et tal, fx 2833.");return;}
            try{const r=await apiKald({a:"mithfidgem",token:fbaSess().token,u:u,mid:mid});
                if(r&&r.ok)aabnBorgerKort(u);else alert((r&&r.fejl)||"Kunne ikke gemme");}catch(e){}
        };
        const bkBid=document.getElementById("bkBid");
        if(bkBid)bkBid.onclick=async()=>{
            const bid=(window.prompt("Kontaktpersonens brugere-id i mitHF (bruges til borgerens login/SSO, fx 5813).\nTomt felt fjerner login-adgangen:",j.brugereId||"")||"").trim();
            if(bid===(j.brugereId||""))return;
            if(bid&&!/^\d{1,8}$/.test(bid)){alert("brugere-id er et tal, fx 5813.");return;}
            try{const r=await apiKald({a:"brugereidgem",token:fbaSess().token,u:u,bid:bid});
                if(r&&r.ok)aabnBorgerKort(u);else alert((r&&r.fejl)||"Kunne ikke gemme");}catch(e){}
        };
        const _aabnKp=()=>aabnKpKort(u,j.kp||{},j.stamNavn||j.navn||j.u);
        const bkKp=document.getElementById("bkKp");if(bkKp)bkKp.onclick=_aabnKp;
        const bkKpRet=document.getElementById("bkKpRet");if(bkKpRet)bkKpRet.onclick=_aabnKp;
        const bkKons=document.getElementById("bkKons");
        if(bkKons)bkKons.onclick=()=>{
            const vis=document.getElementById("bkKonsVis");if(!vis)return;
            vis.innerHTML='<select id="bkKonsVaelg" style="font-family:var(--font);padding:4px 8px;border-radius:8px;border:1px solid rgba(30,55,76,.2);max-width:210px">'+
                '<option value="">— ingen konsulent</option>'+
                ((j.konsListe||[]).map(n=>'<option value="'+fbaEsc(n)+'"'+(n===j.konsulent?" selected":"")+'>'+fbaEsc(n)+'</option>').join(""))+'</select>';
            bkKons.style.display="none";
            document.getElementById("bkKonsVaelg").onchange=async function(){
                try{const r=await apiKald({a:"konssaet",token:fbaSess().token,u:u,navn:this.value});
                    if(r&&r.ok)aabnBorgerKort(u);else alert((r&&r.fejl)||"Kunne ikke skifte");}catch(e){}
            };
        };
    }catch(e){
        const k=document.getElementById("bkKort");
        if(k)k.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — tjek forbindelsen.</p>';
    }
}

/* ===================== 🔧 Admin-central: OMRÅDER (dashboard + undersider) ===================== */
var admOmr="dash",admOmrKlar=false;
const OMR_ELM={   /* element-id → hvilke områder det hører til */
    admGenveje:["dash"],lcaDigest:["dash"],lcaNaevnt:["dash"],admPush:["dash"],
    lcFold:["chat"],lukFold:["chat"],
    svarFold:["feedback"],
    fbaChat:["ai"],videnFold:["ai"],
    assistentWrap:["assistent"],mvidenFold:["assistent"],
    tekstFold:["tekster"],
    supPanel:["support"],
    opretFold:["borgere"],
    hjulFold:["hjul"],
    progWrap:["prog"],
    kalWrap:["kal"],
    notWrap:["noter"],
    dashWrap:["dashboard"],
    ssoWrap:["sso"],
    guiderWrap:["guider"],
    appsWrap:["apps"],
    admRetFold:["ret"],
    admGrupperFold:["grupper"],
    fbaBar:["feedback","borgere"],
    fbaListe:["feedback","borgere","ai","fs","stat"]
};
const OMR_FANER={feedback:["side","ny","borger"],borgere:["adgang","kons","hub"]};
const OMR_AUTOG={ai:"chat",fs:"fs",stat:"stat"};
/* ===================== ⧉ Pop-ud: åbn ét område i sit eget fokuserede vindue ===================== */
function hfPopout(omr,hash){
    if(window.HF_POPOUT)return;   /* fra et pop-ud kan man ikke poppe videre ud */
    var h=hash||"#/feedback-admin";
    var q="?popout=1"+(omr?"&omr="+encodeURIComponent(omr):"");
    var url=location.pathname+q+h;
    var navn="hfpop_"+(omr||h.replace(/[^a-z0-9]/gi,""));
    try{var sess=sessionStorage.getItem("bpa_adm");if(sess)localStorage.setItem("hf_pop_sess",sess);}catch(e){}   /* bro: så pop-ud'et kan arve login */
    window.open(url,navn,"width=1180,height=860,menubar=no,toolbar=no,location=no,status=no");
}
/* Fuldt dublet-univers i nyt vindue — samme adm-univers, fuld navigation, parallelt arbejde */
function hfDuplikat(){
    if(window.HF_POPOUT)return;
    try{var sess=sessionStorage.getItem("bpa_adm");if(sess)localStorage.setItem("hf_pop_sess",sess);}catch(e){}
    window.open(location.pathname+"?dup=1#/feedback-admin","_blank","width=1320,height=920");
}
/* Fast flydende knap-klynge — vises på alle admin-sider, uafhængigt af serverkald */
function poFabVis(id){
    if(window.HF_POPOUT)return;
    var adm=null;try{adm=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
    var paaAdm=(id==="v-fbadmin"||id==="v-intern"||id==="v-nyadmin")&&adm&&adm.token;
    var wrap=document.getElementById("poFab");
    if(!paaAdm){if(wrap)wrap.style.display="none";return;}
    if(!wrap){
        wrap=document.createElement("div");wrap.id="poFab";wrap.className="po-knap";
        wrap.style.cssText="position:fixed;bottom:18px;left:18px;z-index:938;display:flex;gap:8px";
        var mk=function(txt,titel,fn){
            var b=document.createElement("button");b.type="button";b.title=titel;b.textContent=txt;
            b.style.cssText="border:0;border-radius:999px;padding:10px 15px;background:var(--moerk);color:#fff;font-family:var(--font);font-weight:800;font-size:.8rem;cursor:pointer;box-shadow:0 8px 22px rgba(30,55,76,.32)";
            b.onclick=fn;return b;
        };
        wrap.appendChild(mk("⧉ Dette område","Åbn kun dette område i et fokuseret vindue",function(){
            var h=location.hash.replace(/^#\/?/,"").split("?")[0];
            if(h==="intern")hfPopout(null,"#/intern");
            else if(h==="nyheds-admin")hfPopout(null,"#/nyheds-admin");
            else hfPopout(window.admOmr||"dash");
        }));
        wrap.appendChild(mk("🗗 Nyt vindue","Åbn et helt nyt admin-vindue — arbejd i to universer parallelt",hfDuplikat));
        document.body.appendChild(wrap);
    }
    wrap.style.display="flex";
}
/* ===================== 🧩 APPS — interne HF-programmer (modulær, rettighedsstyret) ===================== */
/* Registrér nye programmer her. Hvert program = egen mappe på serveren + egen rettighed.
   url er relativ til universets rod (fx "salg/index.html"). */
var HF_APPS=[
    {id:"salg",ikon:"💰",navn:"HF Salg",beskrivelse:"Salgssystem / CRM — pipeline, kommuner, fokuskommuner og auto-rapporter.",url:"salg/",perm:"salg",sso:true}
];
function hfHarApp(a){return !!(lcaData&&(lcaData.ejer||(lcaData.ret&&lcaData.ret[a.perm])));}
function hfHarNogenApp(){return HF_APPS.some(hfHarApp);}
/* åbn et program i FULDSKÆRM (indlejret iframe) med en elegant svævende retur-knap */
function hfAppAaben(app){
    if(!app||document.getElementById("hfAppOverlay"))return;
    var url=app.url;
    if(app.sso){var sess=fbaSess();var tok=(sess&&sess.token)||"";url+=(url.indexOf("?")>=0?"&":"?")+"t="+encodeURIComponent(tok);}   /* SSO: send session-tokenet med — programmet validerer selv via tjek_session */
    var ov=document.createElement("div");ov.id="hfAppOverlay";
    ov.style.cssText="position:fixed;inset:0;z-index:9998;background:#fff;display:flex;flex-direction:column";
    ov.innerHTML='<iframe src="'+url+'" title="'+fbaEsc(app.navn)+'" style="flex:1;width:100%;height:100%;border:0;display:block"></iframe>'+
        '<button id="hfAppLuk" title="Tilbage til adm-universet" style="position:fixed;top:16px;left:16px;z-index:10000;display:inline-flex;align-items:center;gap:8px;border:0;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.88rem;color:#fff;background:linear-gradient(120deg,var(--moerk),#2a5573);border-radius:999px;padding:10px 18px;box-shadow:0 10px 30px rgba(30,55,76,.4);transition:transform .1s,box-shadow .2s">‹ Adm-universet</button>';
    document.body.appendChild(ov);
    document.body.style.overflow="hidden";
    var luk=document.getElementById("hfAppLuk");
    luk.onmouseenter=function(){this.style.transform="translateY(-1px)";this.style.boxShadow="0 12px 34px rgba(30,55,76,.5)";};
    luk.onmouseleave=function(){this.style.transform="";this.style.boxShadow="0 10px 30px rgba(30,55,76,.4)";};
    luk.onclick=function(){ov.remove();document.body.style.overflow="";};
}
function renderApps(){
    var wrap=document.getElementById("appsWrap");if(!wrap)return;
    var apps=HF_APPS.filter(hfHarApp);
    var h='<div class="fba-bar" style="margin:0 0 12px"><b style="font-size:1.05rem">🧩 Apps</b>'+
        '<span style="color:var(--graa);font-size:.85rem;flex:1">Interne HF-programmer. Du ser kun dem, du har adgang til — de åbner i fuldskærm og lukkes med knappen øverst til venstre.</span></div>';
    if(!apps.length){h+='<div class="fba-kort" style="text-align:center;color:var(--graa);padding:28px">Du har endnu ikke adgang til nogen apps. Adgang gives i ⚙️ Brugere &amp; rettigheder.</div>';}
    else{
        h+='<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(290px,1fr));gap:14px">'+apps.map(function(a){
            return '<div class="hf-app-kort" data-id="'+fbaEsc(a.id)+'" style="background:#fff;border:1px solid rgba(30,55,76,.08);border-radius:18px;padding:18px;cursor:pointer;box-shadow:var(--skygge);display:flex;flex-direction:column;gap:9px">'+
                '<div style="display:flex;align-items:center;gap:13px"><div style="width:54px;height:54px;flex:0 0 auto;border-radius:15px;display:grid;place-items:center;font-size:1.8rem;background:var(--flade);border:1px solid rgba(30,55,76,.08)">'+a.ikon+'</div>'+
                '<div style="min-width:0"><b style="display:block;color:var(--moerk);font-size:1.1rem">'+fbaEsc(a.navn)+'</b>'+(a.status?'<span style="display:inline-block;margin-top:2px;font-size:.7rem;font-weight:800;color:#8a6d00;background:#fff3cd;border-radius:999px;padding:1px 9px">'+fbaEsc(a.status)+'</span>':'')+'</div></div>'+
                '<span style="color:var(--graa);font-size:.88rem;line-height:1.45">'+fbaEsc(a.beskrivelse)+'</span>'+
                '<span style="margin-top:auto;color:var(--blaa);font-weight:800;font-size:.86rem">Åbn i fuldskærm →</span>'+
                '</div>';
        }).join("")+'</div>';
    }
    wrap.innerHTML=h;
    wrap.querySelectorAll(".hf-app-kort").forEach(function(x){x.onclick=function(){hfAppAaben(HF_APPS.find(function(y){return y.id===x.getAttribute("data-id");}));};});
}
/* auto-voksende skrivefelt: højden følger indholdet (til et loft) */
function hfGrow(el,max){if(!el)return;el.style.height="auto";el.style.height=Math.min(el.scrollHeight,max||120)+"px";}
/* ===================== 📖 GUIDER — vejledninger til systemet & programmerne ===================== */
var guiderData=null,guiderAaben=null,guiderRedig=null,guiderForhaandsvis=false;
/* enkel, sikker markdown → HTML (escaper FØRST, så ingen kan injicere kode) */
function guiderMd(src){
    var esc=function(t){return String(t).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");};
    var inline=function(t){
        t=esc(t);
        t=t.replace(/\*\*([^*]+)\*\*/g,"<b>$1</b>");
        t=t.replace(/`([^`]+)`/g,'<code style="background:#eef2f6;border-radius:5px;padding:1px 5px;font-size:.9em">$1</code>');
        t=t.replace(/\[([^\]]+)\]\((https?:[^)\s]+)\)/g,'<a href="$2" target="_blank" rel="noopener" style="color:var(--blaa)">$1</a>');
        return t;
    };
    var emRe=null;try{emRe=new RegExp('^(\\p{Extended_Pictographic}(?:\\u200d\\p{Extended_Pictographic}|\\uFE0F)*)\\s+([\\s\\S]+)$','u');}catch(e){emRe=null;}
    var overskrift=function(txt,id,foerste){
        var em=emRe?txt.match(emRe):null;
        var bord=foerste?"":"padding-top:16px;border-top:1px solid rgba(30,55,76,.10);";
        if(em){return '<div id="'+id+'" style="display:flex;align-items:center;gap:11px;margin:'+(foerste?"4px":"24px")+' 0 8px;'+bord+'scroll-margin-top:80px">'+
            '<span style="width:38px;height:38px;flex:0 0 auto;border-radius:11px;display:grid;place-items:center;font-size:1.15rem;background:var(--flade,#f4f7fa);border:1px solid rgba(30,55,76,.10)">'+em[1]+'</span>'+
            '<span style="color:var(--moerk);font-size:1.18rem;font-weight:800">'+inline(em[2])+'</span></div>';}
        return '<h3 id="'+id+'" style="margin:'+(foerste?"4px":"24px")+' 0 7px;'+bord+'color:var(--moerk);font-size:1.16rem;scroll-margin-top:80px">'+inline(txt)+'</h3>';
    };
    var linjer=String(src||"").replace(/\r\n/g,"\n").split("\n");
    var ud="",lu=null,hi=0;   /* lu: aktiv liste-type ('ul'/'ol'); hi: overskrift-tæller til indholdsfortegnelse */
    var lukListe=function(){if(lu){ud+="</"+lu+">";lu=null;}};
    linjer.forEach(function(ln){
        var t=ln.replace(/\s+$/,"");
        if(/^###\s+/.test(t)){lukListe();ud+='<h4 style="margin:14px 0 5px;color:var(--moerk);font-size:1rem">'+inline(t.replace(/^###\s+/,""))+'</h4>';return;}
        if(/^##\s+/.test(t)){lukListe();ud+=overskrift(t.replace(/^##\s+/,""),"gsec"+hi,hi===0);hi++;return;}
        if(/^#\s+/.test(t)){lukListe();ud+=overskrift(t.replace(/^#\s+/,""),"gsec"+hi,hi===0);hi++;return;}
        if(/^>\s?/.test(t)){lukListe();ud+='<div style="border-left:4px solid var(--blaa);background:#eef6fb;padding:8px 12px;border-radius:8px;margin:6px 0">'+inline(t.replace(/^>\s?/,""))+'</div>';return;}
        if(/^[-*]\s+/.test(t)){if(lu!=="ul"){lukListe();ud+='<ul style="margin:5px 0;padding-left:1.2rem">';lu="ul";}ud+="<li style=\"margin:.2rem 0\">"+inline(t.replace(/^[-*]\s+/,""))+"</li>";return;}
        if(/^\d+\.\s+/.test(t)){if(lu!=="ol"){lukListe();ud+='<ol style="margin:5px 0;padding-left:1.3rem">';lu="ol";}ud+="<li style=\"margin:.2rem 0\">"+inline(t.replace(/^\d+\.\s+/,""))+"</li>";return;}
        if(t.trim()===""){lukListe();return;}
        lukListe();ud+='<p style="margin:.35rem 0;line-height:1.55">'+inline(t)+"</p>";
    });
    lukListe();
    return ud;
}
/* indholdsfortegnelse: samme tælling som guiderMd (## og # tæller, ### springes over) */
function guiderTOC(src){
    var out=[],n=0;
    String(src||"").replace(/\r\n/g,"\n").split("\n").forEach(function(ln){
        var t=ln.replace(/\s+$/,"");
        if(/^###\s+/.test(t))return;
        if(/^##\s+/.test(t)){out.push({id:"gsec"+n,txt:t.replace(/^##\s+/,"")});n++;return;}
        if(/^#\s+/.test(t)){out.push({id:"gsec"+n,txt:t.replace(/^#\s+/,"")});n++;return;}
    });
    return out;
}
async function renderGuider(){
    var wrap=document.getElementById("guiderWrap");if(!wrap)return;
    var sess=fbaSess();if(!sess){wrap.innerHTML="";return;}
    if(!guiderData){
        wrap.innerHTML='<p style="color:var(--graa)">⏳ Henter guides …</p>';
        try{var j=await apiKald({a:"guiderliste",token:sess.token});guiderData=j&&j.ok?j:{guides:[],ret:0};}
        catch(e){wrap.innerHTML='<p style="color:var(--roed)">Kunne ikke hente guides — kræver serveren.</p>';return;}
    }
    var kanRette=!!(guiderData&&guiderData.ret);
    if(guiderRedig){guiderTegnEditor(wrap,kanRette);return;}
    if(guiderAaben){guiderTegnLaeser(wrap,kanRette);return;}
    /* ---- liste ---- */
    var guides=(guiderData&&guiderData.guides)||[];
    var h='<div class="fba-bar" style="margin:0 0 12px"><b style="font-size:1.05rem">📖 Guider</b>'+
        '<span style="color:var(--graa);font-size:.85rem;flex:1">Vejledninger til universet og de programmer, vi bruger.</span>'+
        (kanRette?'<button id="guiderNy" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">➕ Ny guide</button>':'')+'</div>';
    if(!guides.length){
        h+='<div class="fba-kort" style="text-align:center;color:var(--graa);padding:26px">Der er ingen guides endnu.'+(kanRette?' Tryk “➕ Ny guide” for at oprette den første.':'')+'</div>';
    }else{
        h+='<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:12px">';
        guides.forEach(function(g,idx){
            h+='<div class="fba-kort" style="padding:0;overflow:hidden;display:flex;flex-direction:column">'+
                '<div class="guider-kort" data-id="'+fbaEsc(g.id)+'" style="cursor:pointer;padding:16px 16px 12px;flex:1">'+
                '<div style="font-size:1.7rem;line-height:1">'+fbaEsc(g.ikon||"📖")+'</div>'+
                '<b style="display:block;margin:8px 0 4px;color:var(--moerk);font-size:1.02rem">'+fbaEsc(g.titel)+'</b>'+
                '<span style="color:var(--graa);font-size:.86rem;line-height:1.4">'+fbaEsc(g.resume||"")+'</span>'+
                '</div>'+
                '<div style="display:flex;gap:6px;align-items:center;padding:8px 12px;border-top:1px solid rgba(30,55,76,.07);background:#fafcfe">'+
                '<button class="guider-laes" data-id="'+fbaEsc(g.id)+'" style="'+PROG_KNAP+';padding:7px 13px;font-size:.84rem">Læs →</button>'+
                '<span style="flex:1"></span>'+
                (kanRette?('<button class="guider-op" data-id="'+fbaEsc(g.id)+'" title="Flyt op" '+(idx===0?'disabled':'')+' style="border:1px solid var(--kant,rgba(30,55,76,.15));background:#fff;border-radius:8px;width:30px;height:30px;cursor:pointer;'+(idx===0?'opacity:.35':'')+'">↑</button>'+
                    '<button class="guider-ned" data-id="'+fbaEsc(g.id)+'" title="Flyt ned" '+(idx===guides.length-1?'disabled':'')+' style="border:1px solid rgba(30,55,76,.15);background:#fff;border-radius:8px;width:30px;height:30px;cursor:pointer;'+(idx===guides.length-1?'opacity:.35':'')+'">↓</button>'+
                    '<button class="guider-red" data-id="'+fbaEsc(g.id)+'" title="Redigér" style="border:1px solid rgba(30,55,76,.15);background:#fff;border-radius:8px;width:30px;height:30px;cursor:pointer">✏️</button>'):'')+
                '</div>'+
                '</div>';
        });
        h+='</div>';
    }
    wrap.innerHTML=h;
    var ny=document.getElementById("guiderNy");if(ny)ny.onclick=function(){guiderRedig={id:"",ikon:"📖",titel:"",resume:"",md:""};guiderForhaandsvis=false;renderGuider();};
    wrap.querySelectorAll(".guider-kort,.guider-laes").forEach(function(x){x.onclick=function(e){e.stopPropagation();guiderAabnId(x.getAttribute("data-id"));};});
    wrap.querySelectorAll(".guider-red").forEach(function(x){x.onclick=async function(e){e.stopPropagation();await guiderAabnId(x.getAttribute("data-id"),true);};});
    wrap.querySelectorAll(".guider-op").forEach(function(x){x.onclick=function(e){e.stopPropagation();guiderFlyt(x.getAttribute("data-id"),-1);};});
    wrap.querySelectorAll(".guider-ned").forEach(function(x){x.onclick=function(e){e.stopPropagation();guiderFlyt(x.getAttribute("data-id"),1);};});
}
async function guiderAabnId(id,tilRedigering){
    var sess=fbaSess();if(!sess)return;
    try{var j=await apiKald({a:"guidehent",token:sess.token,id:id});
        if(!(j&&j.ok)){alert((j&&j.fejl)||"Kunne ikke hente guiden");return;}
        if(tilRedigering){guiderRedig={id:j.guide.id,ikon:j.guide.ikon,titel:j.guide.titel,resume:j.guide.resume,md:j.guide.md};guiderForhaandsvis=false;}
        else{guiderAaben=j.guide;}
        renderGuider();
    }catch(e){alert("Kunne ikke hente guiden — tjek forbindelsen.");}
}
function guiderTegnLaeser(wrap,kanRette){
    var g=guiderAaben;
    var toc=guiderTOC(g.md);
    var tocHtml=toc.length>=3
        ? '<div style="padding:16px 28px;background:var(--flade);border-bottom:1px solid rgba(30,55,76,.08)">'+
        '<b style="display:block;color:var(--moerk);font-size:.8rem;text-transform:uppercase;letter-spacing:.05em;margin-bottom:8px">Indhold</b>'+
        '<ol style="margin:0;padding-left:1.1rem;columns:2;column-gap:26px;font-size:.9rem">'+
        toc.map(function(s){return '<li style="margin:.15rem 0;break-inside:avoid"><a href="#" data-go="'+s.id+'" style="color:#2a5a78;text-decoration:none">'+fbaEsc(s.txt)+'</a></li>';}).join("")+
        '</ol></div>'
        : '';
    wrap.innerHTML='<button id="guiderRetur" style="'+PROG_KNAP+';margin:0 0 12px">‹ Alle guides</button>'+
        '<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="background:linear-gradient(130deg,var(--moerk),#274a63 60%,var(--blaa));color:#fff;padding:26px 28px">'+
        '<div style="font-size:2.1rem;line-height:1">'+fbaEsc(g.ikon||"📖")+'</div>'+
        '<h2 style="margin:10px 0 4px;font-size:1.5rem">'+fbaEsc(g.titel)+'</h2>'+
        (g.resume?'<p style="margin:0;opacity:.9">'+fbaEsc(g.resume)+'</p>':'')+
        '</div>'+
        tocHtml+
        '<div style="padding:22px 28px;max-width:800px">'+guiderMd(g.md)+'</div>'+
        (kanRette?'<div style="padding:12px 28px;border-top:1px solid rgba(30,55,76,.08);display:flex;gap:8px"><button id="guiderRedKnap" style="'+PROG_KNAP+'">✏️ Redigér denne guide</button></div>':'')+
        '</div>';
    document.getElementById("guiderRetur").onclick=function(){guiderAaben=null;renderGuider();};
    wrap.querySelectorAll("[data-go]").forEach(function(a){a.onclick=function(e){e.preventDefault();var el=document.getElementById(a.getAttribute("data-go"));if(el)el.scrollIntoView({behavior:"smooth",block:"start"});};});
    var rk=document.getElementById("guiderRedKnap");if(rk)rk.onclick=function(){guiderRedig={id:g.id,ikon:g.ikon,titel:g.titel,resume:g.resume,md:g.md};guiderAaben=null;guiderForhaandsvis=false;renderGuider();};
}
function guiderTegnEditor(wrap,kanRette){
    if(!kanRette){wrap.innerHTML='<p style="color:var(--roed)">Du har ikke rettighed til at redigere guides.</p>';return;}
    var g=guiderRedig,erNy=!g.id;
    var F='width:100%;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:10px 12px;font-family:var(--font);font-size:.92rem';
    wrap.innerHTML='<button id="guiderRetur" style="'+PROG_KNAP+';margin:0 0 12px">‹ Fortryd</button>'+
        '<div class="fba-kort" style="max-width:820px">'+
        '<b style="font-size:1.05rem;color:var(--moerk)">'+(erNy?"➕ Ny guide":"✏️ Redigér guide")+'</b>'+
        '<div style="display:flex;gap:10px;margin-top:12px"><div style="flex:0 0 74px"><label style="font-size:.8rem;color:var(--graa)">Ikon</label><input id="gEdIkon" maxlength="4" value="'+fbaEsc(g.ikon||"📖")+'" style="'+F+';text-align:center;font-size:1.3rem"></div>'+
        '<div style="flex:1"><label style="font-size:.8rem;color:var(--graa)">Titel</label><input id="gEdTitel" value="'+fbaEsc(g.titel||"")+'" placeholder="fx “Sådan bruger du Wrike”" style="'+F+'"></div></div>'+
        '<div style="margin-top:10px"><label style="font-size:.8rem;color:var(--graa)">Kort beskrivelse (vises på kortet)</label><input id="gEdResume" value="'+fbaEsc(g.resume||"")+'" placeholder="En sætning om, hvad guiden handler om" style="'+F+'"></div>'+
        '<div style="margin-top:10px;display:flex;align-items:center;gap:10px"><label style="font-size:.8rem;color:var(--graa);flex:1">Indhold <span style="color:#9aa7b2">— brug ## Overskrift, ### Underoverskrift, - punkt, **fed**, [tekst](https://…)</span></label>'+
        '<button id="gEdVis" style="border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:9px;padding:6px 12px;cursor:pointer;font-family:var(--font);font-size:.82rem;font-weight:700">'+(guiderForhaandsvis?"✎ Skriv":"👁 Forhåndsvis")+'</button></div>'+
        (guiderForhaandsvis
            ? '<div style="border:1px solid rgba(30,55,76,.14);border-radius:12px;padding:16px 18px;margin-top:6px;background:#fff;min-height:200px">'+guiderMd(document.getElementById("gEdMd")?document.getElementById("gEdMd").value:g.md)+'</div>'
            : '<textarea id="gEdMd" rows="18" style="'+F+';font-family:ui-monospace,Menlo,Consolas,monospace;font-size:.86rem;line-height:1.5;resize:vertical;margin-top:6px">'+fbaEsc(g.md||"")+'</textarea>')+
        '<div style="display:flex;gap:8px;align-items:center;margin-top:14px">'+
        '<button id="gEdGem" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">💾 Gem guide</button>'+
        '<span id="gEdFejl" style="color:var(--roed);font-size:.85rem"></span><span style="flex:1"></span>'+
        (erNy?'':'<button id="gEdSlet" style="border:1px solid #e4b4b4;background:#fff;color:#b3261e;border-radius:10px;padding:9px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">🗑 Slet</button>')+
        '</div>'+
        '</div>';
    var gemUdkast=function(){var m=document.getElementById("gEdMd");if(m)g.md=m.value;
        var i=document.getElementById("gEdIkon"),t=document.getElementById("gEdTitel"),r=document.getElementById("gEdResume");
        if(i)g.ikon=i.value;if(t)g.titel=t.value;if(r)g.resume=r.value;};
    document.getElementById("guiderRetur").onclick=function(){guiderRedig=null;guiderForhaandsvis=false;renderGuider();};
    document.getElementById("gEdVis").onclick=function(){gemUdkast();guiderForhaandsvis=!guiderForhaandsvis;renderGuider();};
    document.getElementById("gEdGem").onclick=async function(){
        gemUdkast();
        var fejl=document.getElementById("gEdFejl");
        if(!(g.titel||"").trim()){fejl.textContent="Guiden mangler en titel.";return;}
        if((g.md||"").trim().length<20){fejl.textContent="Indholdet er meget kort — skriv lidt mere.";return;}
        this.disabled=true;
        try{var sess=fbaSess();var r=await apiKald({a:"guidegem",token:sess.token,id:g.id||"",ikon:g.ikon||"📖",titel:g.titel,resume:g.resume||"",md:g.md});
            if(r&&r.ok){guiderData=null;guiderRedig=null;guiderForhaandsvis=false;renderGuider();}
            else{this.disabled=false;fejl.textContent=(r&&r.fejl)||"Kunne ikke gemme.";}}
        catch(e){this.disabled=false;fejl.textContent="Kunne ikke gemme — tjek forbindelsen.";}
    };
    var sl=document.getElementById("gEdSlet");
    if(sl)sl.onclick=async function(){
        if(!confirm("Slet guiden “"+(g.titel||"")+"”?\nDen fjernes for alle."))return;
        this.disabled=true;
        try{var sess=fbaSess();var r=await apiKald({a:"guideslet",token:sess.token,id:g.id});
            if(r&&r.ok){guiderData=null;guiderRedig=null;renderGuider();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke slette.");}}
        catch(e){this.disabled=false;alert("Kunne ikke slette — tjek forbindelsen.");}
    };
}
async function guiderFlyt(id,retning){
    if(!guiderData||!guiderData.guides)return;
    var arr=guiderData.guides.slice(),i=arr.findIndex(function(g){return g.id===id;});
    if(i<0)return;var j=i+retning;if(j<0||j>=arr.length)return;
    var tmp=arr[i];arr[i]=arr[j];arr[j]=tmp;
    guiderData.guides=arr;renderGuider();   /* optimistisk */
    try{var sess=fbaSess();await apiKald({a:"guiderorden",token:sess.token,orden:arr.map(function(g){return g.id;})});}
    catch(e){guiderData=null;renderGuider();}
}
function visOmraade(o){
    if(o==="hubadm"){hubAdmAabn();return;}   /* åbner i egen fane — skifter ikke område */
    admOmr=o;
    const bar=document.getElementById("admOmrBar");
    if(bar)bar.style.display=(o==="dash")?"none":"";   /* piller kun inde i områderne — Overblik bruger kortene */
    if(bar)bar.querySelectorAll("button[data-o]").forEach(b=>b.classList.toggle("aktiv",b.getAttribute("data-o")===o));
    Object.keys(OMR_ELM).forEach(id=>{
        const el=document.getElementById(id);
        if(el)el.style.display=OMR_ELM[id].indexOf(o)>=0?"":"none";
    });
    ["lcFold","lukFold","svarFold","videnFold","mvidenFold","hjulFold","admRetFold","opretFold"].forEach(id=>{
        const el=document.getElementById(id);
        if(el&&OMR_ELM[id].indexOf(o)>=0)el.open=true;   /* fold står åben i sit eget område */
    });
    const faner=OMR_FANER[o],fb=document.getElementById("fbaBar");
    if(fb){
        fb.querySelectorAll("button[data-g]").forEach(b=>{
            b.style.display=(faner&&faner.indexOf(b.getAttribute("data-g"))>=0)?"":"none";
        });
        const visH=fb.querySelector(".fba-vis"),opd=document.getElementById("fbaOpdater");
        if(visH)visH.style.display=o==="feedback"?"":"none";
        if(opd)opd.style.display=faner?"":"none";
    }
    if(faner){
        if(faner.indexOf(fbaG)<0)fbaG=faner[0];
        if(fb)fb.querySelectorAll("button[data-g]").forEach(x=>x.classList.toggle("aktiv",x.getAttribute("data-g")===fbaG));
        renderFba();
    }else if(OMR_AUTOG[o]){fbaG=OMR_AUTOG[o];renderFba();}
    if(o==="dash")renderGenveje();
    if(o==="dash"){try{renderDigest();}catch(e){}}   /* gen-tegn ☀️-rækken (frist-chips) ved hver Overblik-visning — fanger ændrede/fjernede frister uden reload */
    if(o==="dash")renderPush();
    if(o==="tekster")renderTekster();
    if(o==="support")renderSupportAdm();
    if(o==="prog")renderProgrammer();
    if(o==="kal")renderKalender();
    if(o==="noter")renderNoter();
    if(o==="grupper")renderGrupper();
    if(o==="dashboard")renderDashboard();
    if(o==="sso")renderSSOAdgang();
    if(o==="guider")renderGuider();
    if(o==="apps")renderApps();
    if(o==="assistent")renderAssistentFane();
}
/* 🎓 Hub-overblik: åbner læringshubbens Adminoverblik DIREKTE i egen fane
   (admin-login følger med automatisk — hubben viser selv overblikket) */
function hubAdmAabn(){
    if(window.HFVin&&window.HFVin.aabn)window.HFVin.aabn("hf-hubadm","laeringshub/index.html","","Hub-overblik");
    else window.open("laeringshub/index.html","_blank");
}
/* ===================== Sektioner — Admin-centralens struktur (redigérbar) ===================== */
var sekData=null,sekKladde=null;   /* sekKladde: redigerings-udkast (null = editor lukket) */
const OMR_DEF={   /* alle kendte områder: [ikon, navn, evt. href] — nye tilføjes HER + i stdSektioner */
    chat:["💬","Live-chat"],feedback:["📬","Feedback"],support:["💬","Beskeder & support"],fs:["🌍","Fællesskab"],ai:["🤖","AI & viden"],assistent:["🤝","HF-assistent"],tekster:["✏️","Tekster"],
    borgere:["👥","Brugere"],hjul:["🗓","Årshjul"],stat:["📊","Tilfredshed"],kal:["📆","Kalender"],
    hubadm:["🎓","Hub-overblik"],
    noter:["📌","Noter"],prog:["🧰","Programmer"],dashboard:["📈","Dashboard"],sso:["🔑","mitHF-adgang"],guider:["📖","Guider"],apps:["🧩","Apps"],
    intern:["🗨","Intern","#/intern"],nyheder:["📣","Nyheder","#/nyheds-admin"],
    grupper:["🏷️","Grupper"],
    ret:["⚙️","Rettigheder"]};
async function sekHent(){
    if(sekData)return sekData;
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"sektioner",token:sess.token});
        if(j&&j.ok){sekData=j;return sekData;}}catch(e){}
    return null;
}
function sekOmrVis(o){   /* må området overhovedet vises for mig? */
    if(o==="ret")return !!(lcaData&&lcaData.ejer);
    if(o==="assistent")return !!(lcaData&&lcaData.ret&&lcaData.ret.assistent);
    if(o==="tekster")return !!(lcaData&&lcaData.ret&&lcaData.ret.tekster);
    if(o==="support")return !!(lcaData&&lcaData.ret&&lcaData.ret.support);
    if(o==="grupper")return !!(lcaData&&lcaData.ret&&lcaData.ret.taggrupper);
    if(o==="dashboard")return !!(lcaData&&lcaData.ret&&lcaData.ret.dashboard);
    if(o==="sso")return !!(lcaData&&lcaData.ret&&lcaData.ret.sso);
    if(o==="apps")return (typeof hfHarNogenApp==="function")?hfHarNogenApp():false;
    return !!OMR_DEF[o];
}
function admBarByg(){   /* områdebjælken m. sektions-etiketter — ud fra opsætningen */
    const bar=document.getElementById("admOmrBar");
    if(!bar||!sekData)return;
    const knap=o=>{const def=OMR_DEF[o];
        return def[2]?'<button data-href="'+def[2]+'">'+def[0]+' '+def[1]+'</button>'
            :'<button data-o="'+o+'"'+(admOmr===o?' class="aktiv"':'')+'>'+def[0]+' '+def[1]+'</button>';};
    const etiket=t=>'<span style="font-size:.66rem;font-weight:800;letter-spacing:.08em;color:var(--graa);text-transform:uppercase;align-self:center;margin:0 2px 0 10px;white-space:nowrap">'+fbaEsc(t)+'</span>';
    let h='<button data-o="dash"'+(admOmr==="dash"?' class="aktiv"':'')+'>🏠 Overblik</button>';
    const brugt={};
    (sekData.sektioner||[]).forEach(s=>{
        const knapper=[];
        (s.omr||[]).forEach(o=>{if(sekOmrVis(o)&&!brugt[o]){brugt[o]=1;knapper.push(o);}});
        if(knapper.length)h+=etiket(s.navn)+knapper.map(knap).join("");
    });
    const rest=Object.keys(OMR_DEF).filter(o=>sekOmrVis(o)&&!brugt[o]);
    if(rest.length)h+=etiket("Mere")+rest.map(knap).join("");
    bar.innerHTML=h;
    bar.querySelectorAll("button[data-o]").forEach(b=>{b.onclick=()=>visOmraade(b.getAttribute("data-o"));});
    bar.querySelectorAll("button[data-href]").forEach(b=>{b.onclick=()=>{location.hash=b.getAttribute("data-href");};});
}
function renderGenveje(){
    const el=document.getElementById("admGenveje");if(!el||admOmr!=="dash")return;
    const d=lcaData||{};
    const ret=d.ret||{};
    const venter=(d.samtaler||[]).filter(s=>s.status==="venter").length,
        mine=(d.samtaler||[]).filter(s=>s.nyt).length;
    let aabne=null,sla=0;
    if(fbaData){const alle=(fbaData||[]).concat(fbaHub?fbaHub.feedback:[]);
        aabne=alle.filter(b=>!b.done).length;sla=alle.filter(b=>slaOver(b)).length;}
    if(window.hfTael&&typeof window.hfTael.feedback==="number")aabne=window.hfTael.feedback;   /* friskt tal fra 15-sek-pulsen */
    const kortIndre=(ik,titel,info,badge)=>
        (badge?'<span style="position:absolute;top:12px;right:12px;background:var(--roed);color:#fff;font-size:.72rem;font-weight:800;border-radius:999px;padding:2px 9px">'+badge+'</span>':'')+
        '<span class="adm-ik">'+ik+'</span>'+
        '<span style="min-width:0"><b style="display:block;font-size:.95rem">'+titel+'</b>'+
        '<span style="color:var(--graa);font-size:.8rem;line-height:1.35;display:block;margin-top:3px">'+info+'</span></span>';
    const KORT_STIL='background:#fff;border-radius:18px;padding:15px 16px;box-shadow:var(--skygge);border:1px solid rgba(30,55,76,.05);position:relative;display:flex;gap:13px;align-items:flex-start';
    const kort=(o,ik,titel,info,badge)=>'<div class="adm-gv" data-o="'+o+'" style="'+KORT_STIL+';cursor:pointer">'+kortIndre(ik,titel,info,badge)+'</div>';
    const link=(href,ik,titel,info)=>'<a class="adm-gv" href="'+href+'" style="'+KORT_STIL+';text-decoration:none;color:inherit">'+kortIndre(ik,titel,info,"")+'</a>';
    const KORT={   /* dashboard-kort pr. område (perm-skjulte giver tom streng) */
        chat:()=>kort("chat","💬","Live-chat",(d.klar?"🟢 du tager imod chats":"⚫ ikke klar")+" · åbningstider",venter+mine||""),
        feedback:()=>kort("feedback","📬","Feedback",(sla?sla+" over 2 hverdage · ":"")+"beskeder & standardsvar",aabne||""),
        fs:()=>ret.moderation?kort("fs","🌍","Fællesskabet","moderation & HF-opslag",d.fsAfventer||""):"",
        ai:()=>kort("ai","🤖","AI & viden","chat-opsamling · vidensbase · til/fra",""),
        assistent:()=>ret.assistent?kort("assistent","🤝","HF-assistent","spørg om systemer, regler & arbejdsgange",""):"",
        tekster:()=>ret.tekster?kort("tekster","✏️","Tekster","redigér FAQ & tekster på borger-universet",""):"",
        support:()=>ret.support?kort("support","💬","Beskeder & support","borger-henvendelser + skriv til borgere",(window.hfTael&&typeof window.hfTael.beskeder==="number"?window.hfTael.beskeder:d.supUbesvaret)||""):"",
        borgere:()=>kort("borgere","👥","Brugere","borger-adgang · konsulenter · læringshub",""),
        kal:()=>kort("kal","📆","Kalender","Teamup — direkte i universet",""),
        hubadm:()=>kort("hubadm","🎓","Hub-overblik","borgernes aktivitet — åbner i egen fane",""),
        noter:()=>kort("noter","📌","Noter","dine post-it-sedler — også delte",""),
        prog:()=>kort("prog","🧰","Programmer","alle systemer — åbn i egne faner",""),
        hjul:()=>(ret.aarshjul_alle||ret.aarshjul_borger)?kort("hjul","🗓","Årshjul","fælles frister & borger-datoer",""):"",
        stat:()=>ret.stat?kort("stat","📊","Tilfredshed","målingen & resultater",""):"",
        intern:()=>link("#/intern","🗨","Intern","tråde & beskeder med kollegerne"),
        nyheder:()=>link("#/nyheds-admin","📣","Nyheder","skriv & planlæg nyheder"),
        grupper:()=>ret.taggrupper?kort("grupper","🏷️","Grupper","tag-grupper: kontorer, teams & adgang",""):"",
        ret:()=>d.ejer?kort("ret","⚙️","Brugere & rettigheder","grupper, navne og roller",""):"",
        dashboard:()=>ret.dashboard?kort("dashboard","📈","Dashboard","hvem er online nu + aktivitets-statistik",""):"",
        sso:()=>ret.sso?kort("sso","🔑","mitHF-adgang","hvem må logge ind i mitHF fra universet",""):"",
        guider:()=>kort("guider","📖","Guider","vejledninger til systemet & programmerne",""),
        apps:()=>HF_APPS.some(a=>d.ejer||ret[a.perm])?kort("apps","🧩","Apps","interne HF-programmer — fx HF Salg",""):""
    };
    const GITTER=k=>'<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:12px;margin:4px 0 14px">'+k+'</div>';
    const OVERSKRIFT=t=>'<h4 class="adm-sek-h">'+fbaEsc(t)+'</h4>';
    /* 🧰 hurtig-start ligger ØVERST — programmerne fyldes i asynkront nedenfor */
    let h='<div class="adm-strip" id="admProgStrip"><span style="font-size:.78rem;font-weight:800;color:var(--graa)">🧰 Hurtig-start</span><span style="color:var(--graa);font-size:.78rem">henter …</span></div>';
    if(sekData&&(sekData.sektioner||[]).length){
        const brugt={};
        sekData.sektioner.forEach(s=>{
            const dele=[];
            (s.omr||[]).forEach(o=>{if(KORT[o]&&!brugt[o]){brugt[o]=1;dele.push(KORT[o]());}});
            const kk=dele.join("");
            if(kk)h+=OVERSKRIFT(s.navn)+GITTER(kk);
        });
        const rest=Object.keys(KORT).filter(o=>!brugt[o]).map(o=>KORT[o]()).join("");
        if(rest)h+=OVERSKRIFT("Mere")+GITTER(rest);
    }else{
        h+=GITTER(Object.keys(KORT).map(o=>KORT[o]()).join(""));   /* h+= (IKKE h=) — strippen øverst skal overleve */
    }
    h+=(sekData&&sekData.ret?'<div style="margin:0 0 12px"><button id="sekTilpas" style="'+PROG_KNAP+'">'+(sekKladde?"‹ Luk uden at gemme":"✏️ Tilpas sektioner")+'</button></div>':'')+
        '<div id="sekEdit"></div>'+
        /* ☀️ Min dag: til stede + kalender + ros samlet i ÉN række nederst */
        '<h4 class="adm-sek-h">☀️ Min dag</h4>'+
        '<div class="adm-minidag">'+
        '<div id="admTilstede"></div>'+
        '<div id="admIdag"></div>'+
        '<div id="admKudos"></div>'+
        '</div>';
    el.innerHTML=h;
    /* på Overblik er område-pillerne overflødige (kortene ER navigationen) — de vises inde i områderne */
    const obar=document.getElementById("admOmrBar");
    if(obar&&admOmr==="dash")obar.style.display="none";
    el.querySelectorAll(".adm-gv[data-o]").forEach(k=>{k.onclick=()=>visOmraade(k.getAttribute("data-o"));});   /* kun kort m. data-o — link-kortene (Intern/Nyheder) navigerer selv */
    const st=document.getElementById("sekTilpas");
    if(st)st.onclick=()=>{sekKladde=sekKladde?null:JSON.parse(JSON.stringify(sekData.sektioner||[]));renderGenveje();};
    if(sekKladde)sekEditVis();
    if(!sekData)sekHent().then(x=>{if(x&&admOmr==="dash"){admBarByg();renderGenveje();}});
    else admBarByg();
    renderTilstede();   /* 👋 hvem er hvor + 🎂 mærkedage */
    renderIdag();   /* 📅 dagens Teamup-aftaler */
    renderKudos();   /* 🌟 ros-strømmen */
    if(!window.__notPaaKoert){window.__notPaaKoert=1;setTimeout(function(){try{notFristPaamind();}catch(e){}},1400);}   /* ⏰ deadline-påmindelser + push (én gang pr. load) */
    /* hurtig-start: programmerne som små piller direkte på overblikket (fælles + ⭐ egne) */
    progHentI().then(d2=>{
        const strip=document.getElementById("admProgStrip");
        if(!strip||admOmr!=="dash")return;
        if(!d2){strip.style.display="none";return;}   /* kan programlisten ikke hentes, skjules boksen pænt */
        const _per=(progData&&progData.personlig)||{skjul:[],hsSkjul:[]};const _hs=_per.hsSkjul||[];
        const liste=(progData.syn||[]).filter(p=>p&&p.url&&p.hurtigstart!==0&&_hs.indexOf(p.id)<0)
            .concat((progData.egne||[]).filter(p=>p&&p.url&&_hs.indexOf(p.id)<0));
        if(!liste.length){strip.style.display="none";return;}
        strip.innerHTML='<span style="font-size:.78rem;font-weight:800;color:var(--graa)">🧰 Hurtig-start:</span>'+
            liste.map((p,ix)=>'<button data-pq="'+ix+'" title="'+fbaEsc(p.besk||p.navn)+'" style="'+PROG_KNAP+';display:inline-flex;align-items:center;gap:6px">'+
                progIkonHTML(p,17)+' '+(p.egen?"⭐ ":"")+fbaEsc(p.navn)+'</button>').join("");
        strip.querySelectorAll("button[data-pq]").forEach(k=>{k.onclick=()=>{
            progAabnI(liste[parseInt(k.getAttribute("data-pq"),10)]);
        };});
    });
}
(function(){
    const bar=document.getElementById("admOmrBar");
    if(!bar)return;
    bar.querySelectorAll("button[data-o]").forEach(b=>{b.onclick=()=>visOmraade(b.getAttribute("data-o"));});
    bar.querySelectorAll("button[data-href]").forEach(b=>{b.onclick=()=>{location.hash=b.getAttribute("data-href");};});
})();
/* ---------- ✏️ sektions-editoren (perm 'sektioner') ---------- */
function sekEditVis(){
    const el=document.getElementById("sekEdit");
    if(!el||!sekKladde)return;
    const alleILi={};sekKladde.forEach(s=>(s.omr||[]).forEach(o=>alleILi[o]=1));
    const frie=Object.keys(OMR_DEF).filter(o=>!alleILi[o]);
    const omrNavn=o=>OMR_DEF[o]?OMR_DEF[o][0]+" "+OMR_DEF[o][1]:o;
    let h='<div style="background:#fff;border-radius:14px;padding:16px;box-shadow:var(--skygge);margin:0 0 14px">'+
        '<b style="font-size:.9rem">✏️ Tilpas sektionerne</b>'+
        '<p style="color:var(--graa);font-size:.75rem;margin:4px 0 12px">Omdøb, flyt og sortér — ændringerne gælder for ALLE administratorer (bjælken og overblikket). Områder du fjerner, lander under «Mere».</p>'+
        '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:12px">';
    sekKladde.forEach((s,si)=>{
        h+='<div style="border:1px solid rgba(30,55,76,.12);border-radius:12px;padding:12px">'+
            '<div style="display:flex;gap:6px;margin-bottom:8px">'+
            '<input class="se-navn" data-si="'+si+'" value="'+fbaEsc(s.navn)+'" maxlength="40" style="'+PROG_FELT+'">'+
            '<button class="se-op" data-si="'+si+'" title="Flyt sektionen op" style="'+PROG_KNAP+'"'+(si===0?" disabled":"")+'>↑</button>'+
            '<button class="se-slet" data-si="'+si+'" title="Slet sektionen (områderne beholdes under Mere)" style="'+PROG_KNAP+';color:var(--roed)">🗑</button></div>';
        (s.omr||[]).forEach((o,oi)=>{
            if(!OMR_DEF[o])return;
            h+='<div style="display:flex;gap:5px;align-items:center;margin:0 0 5px;font-size:.83rem">'+
                '<span style="flex:1">'+omrNavn(o)+'</span>'+
                '<button class="se-o-op" data-si="'+si+'" data-oi="'+oi+'" title="Ryk op" style="'+PROG_KNAP+';padding:2px 7px"'+(oi===0?" disabled":"")+'>↑</button>'+
                '<button class="se-o-ned" data-si="'+si+'" data-oi="'+oi+'" title="Ryk ned" style="'+PROG_KNAP+';padding:2px 7px"'+(oi===(s.omr.length-1)?" disabled":"")+'>↓</button>'+
                '<select class="se-o-flyt" data-si="'+si+'" data-oi="'+oi+'" title="Flyt til anden sektion" style="'+PROG_FELT+';width:auto;padding:3px 6px;font-size:.75rem">'+
                sekKladde.map((s2,si2)=>'<option value="'+si2+'"'+(si2===si?" selected":"")+'>'+fbaEsc(s2.navn.substring(0,18))+'</option>').join("")+
                '<option value="-1">— fjern (til Mere)</option></select></div>';
        });
        h+='</div>';
    });
    h+='</div>';
    if(frie.length)h+='<div style="margin-top:10px;font-size:.8rem;color:var(--graa)"><b>Under «Mere» (ikke placeret):</b> '+
        frie.map(o=>'<span style="white-space:nowrap;margin-right:8px">'+omrNavn(o)+
            ' <select class="se-fri" data-o="'+o+'" style="'+PROG_FELT+';width:auto;padding:2px 5px;font-size:.72rem"><option value="">placér …</option>'+
            sekKladde.map((s2,si2)=>'<option value="'+si2+'">'+fbaEsc(s2.navn.substring(0,18))+'</option>').join("")+'</select></span>').join("")+'</div>';
    h+='<div style="display:flex;gap:8px;margin-top:12px">'+
        '<button id="seTilf" style="'+PROG_KNAP+'">➕ Ny sektion</button>'+
        '<button id="seGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem for alle</button></div></div>';
    el.innerHTML=h;
    const opdater=()=>{sekLaesNavne();sekEditVis();};
    function sekLaesNavne(){el.querySelectorAll(".se-navn").forEach(i=>{const s=sekKladde[parseInt(i.getAttribute("data-si"),10)];if(s)s.navn=i.value;});}
    el.querySelectorAll(".se-op").forEach(k=>{k.onclick=()=>{const si=parseInt(k.getAttribute("data-si"),10);
        if(si>0){sekLaesNavne();const t=sekKladde[si];sekKladde[si]=sekKladde[si-1];sekKladde[si-1]=t;sekEditVis();}};});
    el.querySelectorAll(".se-slet").forEach(k=>{k.onclick=()=>{
        if(!confirm("Slet sektionen? Områderne lander under «Mere»."))return;
        sekLaesNavne();sekKladde.splice(parseInt(k.getAttribute("data-si"),10),1);sekEditVis();};});
    el.querySelectorAll(".se-o-op").forEach(k=>{k.onclick=()=>{const si=+k.getAttribute("data-si"),oi=+k.getAttribute("data-oi");
        const l=sekKladde[si].omr;if(oi>0){sekLaesNavne();const t=l[oi];l[oi]=l[oi-1];l[oi-1]=t;sekEditVis();}};});
    el.querySelectorAll(".se-o-ned").forEach(k=>{k.onclick=()=>{const si=+k.getAttribute("data-si"),oi=+k.getAttribute("data-oi");
        const l=sekKladde[si].omr;if(oi<l.length-1){sekLaesNavne();const t=l[oi];l[oi]=l[oi+1];l[oi+1]=t;sekEditVis();}};});
    el.querySelectorAll(".se-o-flyt").forEach(k=>{k.onchange=()=>{
        const si=+k.getAttribute("data-si"),oi=+k.getAttribute("data-oi"),til=parseInt(k.value,10);
        if(til===si)return;
        sekLaesNavne();
        const o=sekKladde[si].omr.splice(oi,1)[0];
        if(til>=0&&sekKladde[til])sekKladde[til].omr.push(o);
        sekEditVis();};});
    el.querySelectorAll(".se-fri").forEach(k=>{k.onchange=()=>{
        const til=parseInt(k.value,10);
        if(isNaN(til)||!sekKladde[til])return;
        sekLaesNavne();sekKladde[til].omr.push(k.getAttribute("data-o"));sekEditVis();};});
    document.getElementById("seTilf").onclick=()=>{sekLaesNavne();sekKladde.push({id:"",navn:"Ny sektion",omr:[]});sekEditVis();};
    document.getElementById("seGem").onclick=async function(){
        sekLaesNavne();
        const rene=sekKladde.filter(s=>s.navn.trim()!=="");
        if(!rene.length){alert("Der skal være mindst én sektion.");return;}
        this.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"sektionergem",token:sess.token,sektioner:rene});
            if(r&&r.ok){sekData.sektioner=r.sektioner;sekKladde=null;admBarByg();renderGenveje();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
}
/* ===================== ⌨️ Ctrl+K — kommandopaletten (universel søgning) ===================== */
var palAaben=false,palTimer=null,palValgt=0,palRes=[],palQ="";
function palLuk(){const o=document.getElementById("sogPal");if(o)o.remove();palAaben=false;}
function hfPalette(){
    if(palAaben){palLuk();return;}
    if(!fbaSess())return;   /* kun administratorer */
    palAaben=true;palValgt=0;palRes=[];palQ="";
    const o=document.createElement("div");o.id="sogPal";
    o.style.cssText="position:fixed;inset:0;z-index:998;background:rgba(30,55,76,.45);display:flex;align-items:flex-start;justify-content:center;padding:9vh 14px 14px";
    o.innerHTML='<div style="width:min(640px,100%);background:#fff;border-radius:18px;box-shadow:0 30px 80px rgba(30,55,76,.45);overflow:hidden">'+
        '<input id="palInput" autocomplete="off" placeholder="🔎 Søg i alt … kanaler, beskeder, noter, borgere, programmer, områder" style="width:100%;box-sizing:border-box;border:0;outline:none;padding:16px 20px;font-family:var(--font);font-size:1.02rem;border-bottom:1px solid rgba(30,55,76,.1)">'+
        '<div id="palListe" style="max-height:56vh;overflow-y:auto;padding:8px"></div>'+
        '<div style="padding:7px 14px;font-size:.7rem;color:var(--graa);border-top:1px solid rgba(30,55,76,.08)">↑↓ vælg · Enter åbn · Esc luk · genvej: Ctrl+K</div></div>';
    document.body.appendChild(o);
    o.addEventListener("mousedown",e=>{if(e.target===o)palLuk();});
    const inp=document.getElementById("palInput");
    inp.addEventListener("input",()=>{clearTimeout(palTimer);palTimer=setTimeout(palSoeg,220);});
    inp.addEventListener("keydown",e=>{
        if(e.key==="Escape"){palLuk();return;}
        if(e.key==="ArrowDown"){e.preventDefault();palValgt=Math.min(palValgt+1,palRes.length-1);palTegn();}
        else if(e.key==="ArrowUp"){e.preventDefault();palValgt=Math.max(palValgt-1,0);palTegn();}
        else if(e.key==="Enter"){e.preventDefault();if(palRes[palValgt]&&palRes[palValgt].g)palRes[palValgt].g();}
    });
    palSoeg();
    setTimeout(()=>{try{inp.focus();}catch(e){}},60);
}
try{window.hfPalette=hfPalette;}catch(e){}
function palGaaOmr(omr){
    palLuk();
    if(location.hash.replace(/^#\/?/,"")==="feedback-admin"){visOmraade(omr);}
    else{try{sessionStorage.setItem("omr_fokus",omr);}catch(e){}location.hash="#/feedback-admin";}
}
function palGaaIntern(tilstand,fokus){
    palLuk();
    inTilstand=tilstand;try{localStorage.setItem("in_mode",tilstand);}catch(e){}
    if(fokus)try{sessionStorage.setItem("in_fokus",fokus);}catch(e){}
    if(location.hash.replace(/^#\/?/,"")==="intern")renderIntern();
    else location.hash="#/intern";
}
async function palSoeg(){
    const inp=document.getElementById("palInput");if(!inp)return;
    const q=inp.value.trim(),qLo=q.toLowerCase();
    palQ=q;palValgt=0;
    const res=[];
    const match=t=>!qLo||String(t).toLowerCase().indexOf(qLo)>=0;
    /* handlinger */
    [["📌","Ny seddel på notetavlen",()=>{notRedig="ny";palGaaOmr("noter");}],
        ["📢","Ny kanal",()=>{kanOpretVis=true;palGaaIntern("kanaler",null);}],
        ["✍️","Ny privat tråd",()=>{inOpretVis=true;palGaaIntern("traade",null);}]]
        .forEach(h=>{if(match(h[1]))res.push({ik:h[0],t:h[1],s:"",gr:"⚡ Handlinger",g:h[2]});});
    /* områder */
    res.push.apply(res,[["dash","🏠","Overblik"]].concat(Object.keys(OMR_DEF).filter(o=>sekOmrVis(o)).map(o=>[o,OMR_DEF[o][0],OMR_DEF[o][1]]))
        .filter(x=>match(x[2])).slice(0,qLo?6:3)
        .map(x=>({ik:x[1],t:x[2],s:"",gr:"🧭 Områder",g:()=>{const def=OMR_DEF[x[0]];
                if(def&&def[2]){palLuk();location.hash=def[2];}else palGaaOmr(x[0]);}})));
    /* programmer (fælles synlige + ⭐ egne) */
    const progs=await progHentI();
    if(progs&&palQ===q)progAlleSynlige().filter(p=>p&&p.url&&match(p.navn)).slice(0,qLo?5:0)
        .forEach(p=>res.push({ik:p.ikon||"🔗",t:(p.egen?"⭐ ":"")+p.navn,s:"åbn programmet",gr:"🧰 Programmer",g:()=>{palLuk();progAabnI(p);}}));
    /* borgere & kolleger (lokal liste) — fuldt navn fra stam.json når hentet */
    if(!window.STAMNAVN&&!window.__stamHenter){window.__stamHenter=1;const _s=fbaSess();if(_s)apiKald({a:"stamnavne",token:_s.token}).then(j=>{if(j&&j.ok){window.STAMNAVN=j.navne||{};window.STAMSOEG=j.soeg||{};if(palAaben&&palQ===q)palSoeg(q);}}).catch(()=>{});}
    if(qLo.length>=2&&typeof adgangsBorgere==="function"){
        const SN=window.STAMNAVN||{},SS=window.STAMSOEG||{};
        adgangsBorgere(true).filter(b=>match(b.n)||match(b.u)||(SN[b.u]&&match(SN[b.u]))||(SS[b.u]&&match(SS[b.u]))).slice(0,5)
            .forEach(b=>{const erBorger=/^20\d{4}$/.test(b.u);const fuldt=SN[b.u]||b.n||b.u;
                res.push({ik:erBorger?"🧑‍🦽":"👤",av:"mithf-api.php?a=pfoto&u="+encodeURIComponent(String(b.u).toLowerCase()),t:fuldt+" ("+b.u+")",
                    s:erBorger?"åbn borger-kortet 360°":"kollega — åbn stamkortet",gr:"👥 Borgere & kolleger",
                    g:()=>{palLuk();aabnBorgerKort(b.u);}});});
    }
    palRes=res;palTegn();   /* vis de lokale fund straks … */
    if(qLo.length<2)return;
    /* … og hent server-fundene bagefter */
    const sess=fbaSess();
    try{
        const j=await apiKald({a:"soegadm",token:sess.token,q:q});
        if(!j||!j.ok||palQ!==q||!palAaben)return;
        (j.kanaler||[]).forEach(k=>res.push({ik:"📢",t:k.titel+(k.arkiv?" (arkiveret)":""),s:k.uddrag,gr:"📢 Kanaler",g:()=>palGaaIntern("kanaler","kan:"+k.id)}));
        (j.beskeder||[]).forEach(b=>res.push({ik:"💬",t:"i "+b.titel,s:b.uddrag,gr:"💬 Kanal-beskeder",g:()=>palGaaIntern("kanaler","kan:"+b.id)}));
        (j.traade||[]).forEach(t=>res.push({ik:"✉️",t:t.titel,s:t.uddrag,gr:"✉️ Private tråde",g:()=>palGaaIntern("traade",t.id)}));
        (j.noter||[]).forEach(n=>res.push({ik:"📌",t:n.titel,s:n.uddrag,gr:"📌 Noter",g:()=>palGaaOmr("noter")}));
        palRes=res;palTegn();
    }catch(e){}
}
function palTegn(){
    const el=document.getElementById("palListe");if(!el)return;
    if(!palRes.length){el.innerHTML='<p style="color:var(--graa);font-size:.88rem;padding:10px 12px">'+(palQ.length>=2?"Ingen fund på «"+fbaEsc(palQ)+"» …":"Skriv for at søge — eller vælg en handling.")+'</p>';return;}
    let h="",sidsteGr="";
    palRes.forEach((r,ix)=>{
        if(r.gr!==sidsteGr){sidsteGr=r.gr;h+='<div style="font-size:.68rem;font-weight:800;letter-spacing:.08em;text-transform:uppercase;color:var(--graa);padding:8px 12px 3px">'+r.gr+'</div>';}
        h+='<div class="pal-r" data-ix="'+ix+'" style="display:flex;gap:10px;align-items:center;padding:8px 12px;border-radius:10px;cursor:pointer;'+(ix===palValgt?'background:#eaf4fb':'')+'">'+
            (r.av
                ? '<span style="position:relative;width:26px;height:26px;border-radius:50%;overflow:hidden;flex:none;display:grid;place-items:center;font-size:1rem;background:#eef4f8">'+r.ik+'<img class="hf-av-img" src="'+r.av+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0"></span>'
                : '<span style="font-size:1.05rem;flex:none">'+r.ik+'</span>')+
            '<span style="min-width:0"><b style="font-size:.9rem">'+fbaEsc(r.t)+'</b>'+
            (r.s?'<span style="display:block;color:var(--graa);font-size:.76rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">'+fbaEsc(r.s)+'</span>':'')+'</span></div>';
    });
    el.innerHTML=h;
    if(typeof hfAvatarWire==="function")hfAvatarWire(el);
    el.querySelectorAll(".pal-r").forEach(r=>{
        /* mousedown (ikke click): hover må ikke nå at gen-tegne listen under kliket */
        r.onmousedown=e=>{e.preventDefault();const x=palRes[parseInt(r.getAttribute("data-ix"),10)];if(x&&x.g)x.g();};
        r.onmouseenter=()=>{   /* kun markering — ALDRIG fuld gen-tegning ved hover */
            palValgt=parseInt(r.getAttribute("data-ix"),10);
            el.querySelectorAll(".pal-r").forEach(q=>{q.style.background=q===r?"#eaf4fb":"";});
        };
    });
    const akt=el.querySelector('.pal-r[data-ix="'+palValgt+'"]');
    if(akt&&akt.scrollIntoView)akt.scrollIntoView({block:"nearest"});
}
document.addEventListener("keydown",e=>{
    if((e.ctrlKey||e.metaKey)&&(e.key==="k"||e.key==="K")){
        if(!fbaSess())return;
        e.preventDefault();hfPalette();
    }
});
/* topbar-chippen «Tager imod chats / Ikke klar» (#abKlar, bygges i univers-retur.js) opdaterer ikke
   selv den grønne/grå markering i Live-chat-flisen + panelet → gør det her efter klik. */
document.addEventListener("click",function(e){
    var b=e.target&&e.target.closest&&e.target.closest("#abKlar");if(!b)return;
    /* læs ny status fra selve knap-teksten (den opdateres pålideligt): «🟢 Tager imod chats»=klar */
    function opdaterKlar(){
        var nyKlar=/tager imod/i.test(b.textContent||"")?1:0;
        try{if(typeof lcaData!=="undefined"&&lcaData)lcaData.klar=nyKlar;}catch(_){}
        try{if(typeof lcaRenderListe==="function"&&document.getElementById("lcaBoks"))lcaRenderListe();}catch(_){}
        try{if(typeof renderGenveje==="function")renderGenveje();}catch(_){}
    }
    setTimeout(opdaterKlar,800);setTimeout(opdaterKlar,1700);
});
try{if(sessionStorage.getItem("pal_aaben")==="1"){sessionStorage.removeItem("pal_aaben");if(fbaSess())setTimeout(hfPalette,500);}}catch(e){}
/* ===================== 🧰 Programmer — fælles liste (gruppe-styret) + ⭐ egne ===================== */
var progData=null,progRet=0,progRedig=false,progEgenRedig=false,progTilpas=false;
async function progHentI(frisk){
    if(progData&&!frisk)return progData;
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"programmer",token:sess.token});
        if(j&&j.ok){progData={syn:j.programmer||[],egne:j.egne||[],alle:j.alle||null,grupper:j.grupper||[],ret:j.ret?1:0,
            personlig:(j.personlig&&typeof j.personlig==="object")?{skjul:j.personlig.skjul||[],hsSkjul:j.personlig.hsSkjul||[]}:{skjul:[],hsSkjul:[]}};
            progRet=progData.ret;return progData;}}catch(e){}
    return null;
}
function progAlleSynlige(){return progData?(progData.syn||[]).concat(progData.egne||[]):[];}
function progLogoSrc(p){   /* logo-felt: billed-URL, 'auto' (favicon fra programmets domæne) el. tom */
    if(!p.logo)return "";
    if(p.logo==="auto"){try{return "https://icons.duckduckgo.com/ip3/"+new URL(p.url).hostname+".ico";}catch(e){return "";}}
    return p.logo;
}
function progIkonHTML(p,px){
    const src=progLogoSrc(p);
    const emoji='<span style="font-size:'+Math.round(px*.82)+'px;line-height:1'+(src?';display:none':'')+'">'+fbaEsc(p.ikon||"🔗")+'</span>';
    if(!src)return emoji;
    return '<img src="'+fbaEsc(src)+'" alt="" loading="lazy" style="width:'+px+'px;height:'+px+'px;border-radius:22%;object-fit:contain;flex:none" '+
        'onerror="this.style.display=\'none\';if(this.nextElementSibling)this.nextElementSibling.style.display=\'\'">'+emoji;
}
function progAabnI(p){
    if(!p||!p.url)return;
    if(p.nyfane){window.open(p.url,"_blank");return;}   /* programmet isolerer sig (fx e-conomic) — genbrug umulig */
    const navn="hf-prog-"+p.id;   /* EGEN plads — borgernes mitHF-app-vindue (mithf-bpa) røres ikke */
    if(window.HFVin&&window.HFVin.aabn)window.HFVin.aabn(navn,p.url,"",p.navn);
    else window.open(p.url,"_blank");
}
const PROG_KNAP='border:1px solid rgba(30,55,76,.16);background:#f4f8fb;border-radius:9px;cursor:pointer;font-family:var(--font);font-size:.8rem;font-weight:800;color:var(--moerk);padding:6px 12px';
function progKnapper(p,ix){
    return '<button data-px="'+ix+'" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">Åbn →</button>';
}
const PROG_FELT='border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:7px 10px;font-family:var(--font);font-size:.85rem;width:100%;box-sizing:border-box';
function progKortHTML(p,ix){   /* visnings-kort (bruges til både fælles og egne) */
    return '<div style="background:#fff;border-radius:16px;padding:16px 18px;box-shadow:var(--skygge)">'+
        '<div style="display:flex;align-items:center;gap:10px">'+progIkonHTML(p,26)+'<b>'+fbaEsc(p.navn)+'</b></div>'+
        (p.besk?'<p style="color:var(--graa);font-size:.82rem;margin:6px 0 10px">'+fbaEsc(p.besk)+'</p>':'<div style="height:10px"></div>')+
        '<div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center">'+progKnapper(p,ix)+
        (p.nyfane?'<span style="color:var(--graa);font-size:.74rem">åbner altid i ny fane</span>':'')+'</div>'+
        (p.guide?'<details style="margin-top:10px"><summary style="cursor:pointer;font-size:.82rem;font-weight:800;color:var(--blaa)">📖 Guide & beskrivelse</summary>'+
            '<div style="font-size:.85rem;white-space:pre-wrap;margin-top:6px;color:var(--moerk)">'+fbaEsc(p.guide)+'</div></details>':'')+
        '</div>';
}
function progFelterHTML(p,ix,klasse,medSynlighed){   /* redigerings-felter (fælles: pr-*, egne: eg-*) */
    const GN=(typeof GRP_NAVNE!=="undefined")?GRP_NAVNE:{};
    return '<div class="'+klasse+'-kort" data-ix="'+ix+'" style="background:#fff;border-radius:16px;padding:16px;box-shadow:var(--skygge);display:grid;gap:7px">'+
        '<div style="display:flex;gap:7px"><input class="'+klasse+'-ikon" value="'+fbaEsc(p.ikon)+'" maxlength="8" title="Ikon (emoji) — vises hvis der ikke er logo" style="'+PROG_FELT+';width:64px;text-align:center;flex:none">'+
        '<input class="'+klasse+'-navn" value="'+fbaEsc(p.navn)+'" maxlength="60" placeholder="Navn" style="'+PROG_FELT+'"></div>'+
        '<input class="'+klasse+'-url" value="'+fbaEsc(p.url)+'" maxlength="300" placeholder="https://…" style="'+PROG_FELT+'">'+
        '<input class="'+klasse+'-logo" value="'+fbaEsc(p.logo||"")+'" maxlength="300" placeholder="Logo: billed-URL · «auto» = hent fra hjemmesiden · tom = emoji" style="'+PROG_FELT+'">'+
        '<input class="'+klasse+'-besk" value="'+fbaEsc(p.besk||"")+'" maxlength="200" placeholder="Kort beskrivelse" style="'+PROG_FELT+'">'+
        (medSynlighed?'<textarea class="'+klasse+'-guide" rows="3" placeholder="Guide til programmet — login-tips, hvornår det bruges, gode råd …" style="'+PROG_FELT+';resize:vertical">'+fbaEsc(p.guide||"")+'</textarea>'+
            '<div style="border:1px dashed rgba(30,55,76,.2);border-radius:10px;padding:9px">'+
            '<b style="font-size:.78rem">👁 Synlig for grupperne</b> <span style="font-size:.72rem;color:var(--graa)">(ingen flueben = alle ser programmet)</span>'+
            '<div style="display:flex;gap:9px;flex-wrap:wrap;margin:6px 0 4px">'+
            ((progData&&progData.grupper)||[]).map(g=>'<label style="font-size:.78rem;display:flex;gap:4px;align-items:center"><input type="checkbox" class="pr-grp" data-g="'+fbaEsc(g)+'"'+((p.grupper||[]).indexOf(g)>=0?" checked":"")+'> '+fbaEsc(GN[g]||g)+'</label>').join("")+'</div>'+
            '<b style="font-size:.78rem">Undtagelser pr. bruger</b> <span style="font-size:.72rem;color:var(--graa)">(klik chip: ✔ vis → ✕ skjul → fjern)</span>'+
            '<div class="pr-over-chips" data-ix="'+ix+'" style="display:flex;gap:5px;flex-wrap:wrap;margin:5px 0">'+
            Object.keys(p.over||{}).map(l=>'<button class="pr-ov-chip" data-ix="'+ix+'" data-l="'+fbaEsc(l)+'" style="'+PROG_KNAP+';padding:2px 9px;font-size:.74rem'+(p.over[l]?'':';color:var(--roed)')+'">'+(p.over[l]?"✔ ":"✕ ")+fbaEsc(visA(l))+'</button>').join("")+'</div>'+
            '<input class="pr-ov-soeg" data-ix="'+ix+'" placeholder="🔎 Tilføj undtagelse — søg kollega …" style="'+PROG_FELT+';max-width:250px;font-size:.76rem;padding:4px 8px">'+
            '<div class="pr-ov-hits" data-ix="'+ix+'" style="display:flex;gap:5px;flex-wrap:wrap;margin-top:4px"></div></div>':'')+
        '<div style="display:flex;gap:8px;align-items:center;justify-content:space-between;flex-wrap:wrap">'+
        '<label style="font-size:.78rem;color:var(--graa);display:flex;align-items:center;gap:6px"><input type="checkbox" class="'+klasse+'-nyfane"'+(p.nyfane?" checked":"")+'> Åbner altid i ny fane</label>'+
        (klasse==="pr"?'<label style="font-size:.78rem;color:var(--graa);display:flex;align-items:center;gap:6px" title="Vis som standard-pille i Hurtig-start øverst (den enkelte kan selv fravælge)"><input type="checkbox" class="pr-hs"'+((p.hurtigstart===undefined||p.hurtigstart)?" checked":"")+'> ⭐ Standard i Hurtig-start</label>':'')+
        '<div style="display:flex;gap:8px;margin-left:auto">'+
        (klasse==="eg"?'<button class="eg-gem" data-ix="'+ix+'" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem</button>':'')+
        '<button class="'+klasse+'-slet" data-ix="'+ix+'" style="'+PROG_KNAP+';color:var(--roed)">🗑 Fjern</button>'+
        '</div></div></div>';
}
function progTilpasHTML(d){   /* ALLE admins: vælg selv hvad du vil se i listen + Hurtig-start */
    var skjul=((d.personlig&&d.personlig.skjul)||[]),hs=((d.personlig&&d.personlig.hsSkjul)||[]);
    return '<div style="background:#fff;border-radius:16px;padding:16px 18px;box-shadow:var(--skygge);max-width:560px">'+
        '<b style="font-size:.95rem">🎛 Vælg hvad DU vil se</b>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 12px">Fjern fluebenet ved de programmer, du ikke bruger — det ændrer kun DIN visning. «Hurtig-start» er pillerne øverst på dit overblik.</p>'+
        '<div style="display:flex;gap:10px;font-size:.68rem;font-weight:800;color:var(--graa);padding:0 4px 4px"><span style="flex:1"></span><span style="width:56px;text-align:center">I listen</span><span style="width:80px;text-align:center">Hurtig-start</span></div>'+
        (d.syn||[]).map(function(p){
            var erHs=p.hurtigstart!==0;
            return '<div style="display:flex;gap:10px;align-items:center;padding:7px 4px;border-top:1px solid rgba(30,55,76,.07)">'+
                '<span style="flex:1;display:flex;align-items:center;gap:8px;min-width:0">'+progIkonHTML(p,20)+'<span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.88rem">'+fbaEsc(p.navn)+'</span></span>'+
                '<span style="width:56px;text-align:center"><input type="checkbox" class="ptp-liste" data-id="'+fbaEsc(p.id)+'"'+(skjul.indexOf(p.id)<0?" checked":"")+'></span>'+
                '<span style="width:80px;text-align:center">'+(erHs?'<input type="checkbox" class="ptp-hs" data-id="'+fbaEsc(p.id)+'"'+(hs.indexOf(p.id)<0?" checked":"")+'>':'<span style="color:var(--graa);font-size:.72rem" title="Ikke valgt som standard i Hurtig-start">—</span>')+'</span>'+
                '</div>';
        }).join("")+
        '<button id="progTilpasGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12;margin-top:14px">💾 Gem min visning</button>'+
        '</div>';
}
async function renderProgrammer(){
    const el=document.getElementById("progWrap");if(!el)return;
    if(!progData)el.innerHTML='<p style="color:var(--graa)">⏳ Henter programmer …</p>';
    const d=await progHentI();
    if(admOmr!=="prog")return;
    if(!d){el.innerHTML='<p style="color:var(--graa)">Kunne ikke hente programlisten — prøv at genindlæse siden.</p>';return;}
    const OVERSKRIFT=t=>'<h4 style="margin:14px 0 8px;font-size:.76rem;letter-spacing:.08em;text-transform:uppercase;color:var(--graa)">'+t+'</h4>';
    const GRID=(k,bred)=>'<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax('+(bred?'330px':'265px')+',1fr));gap:12px">'+k+'</div>';
    let h='<p class="intro" style="margin-top:4px">Virksomhedens systemer — hvert program åbner i sin <b>egen faste fane</b>. Fælles-listen styres centralt (pr. gruppe m. undtagelser); dine <b>⭐ egne programmer</b> ser kun du. Samme liste ligger bag 🧰 i admin-linjen.</p>'+
        '<div style="margin:0 0 14px;display:flex;gap:8px;flex-wrap:wrap">'+
        (d.ret?'<button id="progRedigBtn" style="'+PROG_KNAP+'">'+(progRedig?"‹ Luk uden at gemme":"✏️ Redigér fælles-listen")+'</button>':'')+
        (progRedig?'<button id="progTilf" style="'+PROG_KNAP+'">➕ Tilføj program</button>'+
            '<button id="progGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem for alle</button>':'')+
        (!progRedig?'<button id="progEgen" style="'+PROG_KNAP+'">'+(progEgenRedig?"‹ Luk egne":"⭐ Redigér mine egne")+'</button>':'')+
        (progEgenRedig&&!progRedig?'<button id="progEgenTilf" style="'+PROG_KNAP+'">➕ Tilføj eget program</button>'+
            '<button id="progEgenGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem mine egne</button>':'')+
        (!progRedig&&!progEgenRedig?'<button id="progTilpasBtn" style="'+PROG_KNAP+'">'+(progTilpas?"‹ Luk tilpasning":"🎛 Vis/skjul for mig")+'</button>':'')+
        '</div>';
    if(progRedig&&d.alle){
        h+=GRID(d.alle.map((p,ix)=>progFelterHTML(p,ix,"pr",true)).join(""),true);
    }else if(progTilpas){
        h+=progTilpasHTML(d);
        h+=OVERSKRIFT("⭐ Egne programmer — kun synlige for dig");
        h+=(d.egne||[]).length?GRID((d.egne||[]).map((p,ix)=>progKortHTML(p,1000+ix)).join(""))
            :'<p style="color:var(--graa);font-size:.85rem">Ingen endnu.</p>';
    }else{
        var _skjul=((d.personlig&&d.personlig.skjul)||[]);
        h+=GRID((d.syn||[]).map((p,ix)=>_skjul.indexOf(p.id)<0?progKortHTML(p,ix):"").join(""));
        if(_skjul.length)h+='<p style="color:var(--graa);font-size:.78rem;margin:6px 2px 0">🙈 '+_skjul.length+' program'+(_skjul.length>1?"mer":"")+' skjult i din visning — tryk «🎛 Vis/skjul for mig» for at ændre.</p>';
        h+=OVERSKRIFT("⭐ Egne programmer — kun synlige for dig");
        if(progEgenRedig){
            h+=(d.egne||[]).length?GRID((d.egne||[]).map((p,ix)=>progFelterHTML(p,ix,"eg",false)).join(""),true):'<p style="color:var(--graa);font-size:.85rem">Ingen endnu — tryk ➕ Tilføj eget program.</p>';
        }else{
            h+=(d.egne||[]).length?GRID((d.egne||[]).map((p,ix)=>progKortHTML(p,1000+ix)).join(""))
                :'<p style="color:var(--graa);font-size:.85rem">Ingen endnu — tryk ⭐ Redigér mine egne og tilføj de sider, DU bruger.</p>';
        }
    }
    el.innerHTML=h;
    el.querySelectorAll("button[data-px]").forEach(k=>{k.onclick=()=>{
        const ix=parseInt(k.getAttribute("data-px"),10);
        progAabnI(ix>=1000?(d.egne||[])[ix-1000]:(d.syn||[])[ix]);
    };});
    const rb=document.getElementById("progRedigBtn");
    if(rb)rb.onclick=()=>{progRedig=!progRedig;progEgenRedig=false;renderProgrammer();};
    const eg=document.getElementById("progEgen");
    if(eg)eg.onclick=()=>{progEgenRedig=!progEgenRedig;renderProgrammer();};
    const tp=document.getElementById("progTilpasBtn");
    if(tp)tp.onclick=()=>{progTilpas=!progTilpas;progRedig=false;progEgenRedig=false;renderProgrammer();};
    const tpg=document.getElementById("progTilpasGem");
    if(tpg)tpg.onclick=async function(){
        var skjul=[],hs=[];
        el.querySelectorAll(".ptp-liste").forEach(c=>{if(!c.checked)skjul.push(c.getAttribute("data-id"));});
        el.querySelectorAll(".ptp-hs").forEach(c=>{if(!c.checked)hs.push(c.getAttribute("data-id"));});
        this.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"progpersonlig",token:sess.token,skjul:skjul,hsSkjul:hs});
            if(r&&r.ok){if(progData&&progData.personlig){progData.personlig.skjul=r.skjul||skjul;progData.personlig.hsSkjul=r.hsSkjul||hs;}progTilpas=false;renderProgrammer();try{renderGenveje();}catch(e){}}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
    const tf=document.getElementById("progTilf");
    if(tf)tf.onclick=()=>{progLaesRedigering();progData.alle.push({id:"",ikon:"🔗",navn:"",url:"",besk:"",guide:"",logo:"",grupper:[],over:{}});renderProgrammer();};
    const gm=document.getElementById("progGem");
    if(gm)gm.onclick=async function(){
        progLaesRedigering();
        const rene=progData.alle.filter(p=>p.navn.trim()!=="");
        if(!rene.length){alert("Listen kan ikke være tom.");return;}
        this.disabled=true;
        const sess=fbaSess();
        try{const r=await apiKald({a:"programgem",token:sess.token,programmer:rene});
            if(r&&r.ok){progRedig=false;await progHentI(true);renderProgrammer();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
    el.querySelectorAll(".pr-slet").forEach(k=>{k.onclick=()=>{
        if(!confirm("Fjern programmet fra fælles-listen?"))return;
        progLaesRedigering();progData.alle.splice(parseInt(k.getAttribute("data-ix"),10),1);renderProgrammer();
    };});
    /* undtagelses-chips: klik = ✔→✕→fjern; søgefelt tilføjer som ✔ */
    el.querySelectorAll(".pr-ov-chip").forEach(k=>{k.onclick=()=>{
        progLaesRedigering();
        const p=progData.alle[parseInt(k.getAttribute("data-ix"),10)],l=k.getAttribute("data-l");
        if(!p.over)p.over={};
        if(p.over[l])p.over[l]=0;else delete p.over[l];
        renderProgrammer();
    };});
    el.querySelectorAll(".pr-ov-soeg").forEach(inp=>{inp.addEventListener("input",()=>{
        const ix=parseInt(inp.getAttribute("data-ix"),10);
        const hits=el.querySelector('.pr-ov-hits[data-ix="'+ix+'"]');
        const q=inp.value.toLowerCase().trim();
        if(!q||q.length<2){hits.innerHTML="";return;}
        const p=progData.alle[ix];
        hits.innerHTML=((lcaData&&lcaData.admins)||[]).filter(l=>!(p.over&&l in p.over))
            .filter(l=>visA(l).toLowerCase().indexOf(q)>=0||l.indexOf(q)>=0).slice(0,8)
            .map(l=>'<button class="pr-ov-tilf" data-ix="'+ix+'" data-l="'+fbaEsc(l)+'" style="'+PROG_KNAP+';padding:2px 9px;font-size:.74rem">➕ '+fbaEsc(visA(l))+'</button>').join("");
        hits.querySelectorAll(".pr-ov-tilf").forEach(k=>{k.onclick=()=>{
            progLaesRedigering();
            const p2=progData.alle[parseInt(k.getAttribute("data-ix"),10)];
            if(!p2.over)p2.over={};
            p2.over[k.getAttribute("data-l")]=1;
            renderProgrammer();
        };});
    });});
    /* egne programmer */
    const et=document.getElementById("progEgenTilf");
    if(et)et.onclick=()=>{progEgneLaes();progData.egne.push({id:"",ikon:"🔗",navn:"",url:"",besk:"",logo:"",nyfane:0,egen:1});renderProgrammer();};
    const egm=document.getElementById("progEgenGem");
    if(egm)egm.onclick=async function(){
        progEgneLaes();
        this.disabled=true;
        const sess=fbaSess();
        try{const r=await apiKald({a:"progegen",token:sess.token,egne:progData.egne.filter(p=>p.navn.trim()!=="")});
            if(r&&r.ok){progEgenRedig=false;await progHentI(true);renderProgrammer();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
    el.querySelectorAll(".eg-slet").forEach(k=>{k.onclick=()=>{
        progEgneLaes();progData.egne.splice(parseInt(k.getAttribute("data-ix"),10),1);renderProgrammer();
    };});
    /* per-kort «💾 Gem» — gemmer alle egne, men BLIVER i redigerings-tilstand (progEgenRedig uændret) */
    el.querySelectorAll(".eg-gem").forEach(k=>{k.onclick=async function(){
        progEgneLaes();
        const nvn=(progData.egne[parseInt(this.getAttribute("data-ix"),10)]||{}).navn||"";
        if(!nvn.trim()){alert("Giv programmet et navn, før du gemmer.");return;}
        this.disabled=true;this.textContent="💾 Gemmer …";
        const sess=fbaSess();
        try{const r=await apiKald({a:"progegen",token:sess.token,egne:progData.egne.filter(p=>p.navn.trim()!=="")});
            if(r&&r.ok){await progHentI(true);renderProgrammer();}
            else{this.disabled=false;this.textContent="💾 Gem";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;this.textContent="💾 Gem";}
    };});
}
function progEgneLaes(){   /* saml egne-felterne ind før gen-tegning/gem */
    document.querySelectorAll("#progWrap .eg-kort").forEach(kort=>{
        const p=progData.egne[parseInt(kort.getAttribute("data-ix"),10)];if(!p)return;
        p.ikon=kort.querySelector(".eg-ikon").value.trim()||"🔗";
        p.navn=kort.querySelector(".eg-navn").value.trim();
        p.url=kort.querySelector(".eg-url").value.trim();
        p.logo=kort.querySelector(".eg-logo").value.trim();
        p.besk=kort.querySelector(".eg-besk").value.trim();
        p.nyfane=kort.querySelector(".eg-nyfane").checked?1:0;
    });
}
/* ===================== 🗓 Kalender — Teamup direkte i universet ===================== */
var kalConf=null;
async function kalHent(){
    if(kalConf)return kalConf;
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"teamupconf",token:sess.token});
        if(j&&j.ok){kalConf=j;return kalConf;}}catch(e){}
    return null;
}
async function renderKalender(){
    const el=document.getElementById("kalWrap");if(!el)return;
    if(!kalConf)el.innerHTML='<p style="color:var(--graa)">⏳ Henter kalender …</p>';
    const c=await kalHent();
    if(admOmr!=="kal")return;
    if(!c){el.innerHTML='<p style="color:var(--graa)">Kunne ikke hente kalender-opsætningen.</p>';return;}
    let h='<p class="intro" style="margin-top:4px">Teamup — hele kalenderen, direkte her i universet. Du kan oprette og flytte aftaler i vinduet nedenfor.</p>'+
        '<div style="margin:0 0 12px;display:flex;gap:8px;flex-wrap:wrap">'+
        '<button id="kalNyFane" style="'+PROG_KNAP+'">↗ Åbn Teamup i egen fane</button>'+
        (c.ret?'<button id="kalOpsBtn" style="'+PROG_KNAP+'">⚙️ Kalender-opsætning</button>':'')+
        '</div>'+
        '<div id="kalOps" style="display:none;background:#fff;border-radius:14px;padding:14px;box-shadow:var(--skygge);margin:0 0 12px;max-width:640px">'+
        '<label style="font-size:.8rem;font-weight:800;color:var(--graa)">Indlejrings-link (kalender-link fra Teamup)</label>'+
        '<input id="kalLink" value="'+fbaEsc(c.link||"")+'" maxlength="300" style="'+PROG_FELT+';margin:4px 0 4px">'+
        '<p style="color:var(--graa);font-size:.75rem;margin:0 0 10px">💡 Logger kalenderen dig ud hele tiden? Det er et <b>konto-login</b>, der timer ud ved inaktivitet. Brug i stedet et <b>delt kalender-link</b>: Teamup → Indstillinger → Del kalender → opret et link, vælg adgang «Ændre» (så man stadig kan oprette/flytte aftaler) → indsæt dét link her. Delte links bærer adgangen i selve linket, så kalenderen aldrig beder om login.</p>'+
        '<label style="font-size:.8rem;font-weight:800;color:var(--graa)">iCalendar-feed (til «I dag»-listen på overblikket)</label>'+
        '<input id="kalIcs" value="'+fbaEsc(c.ics||"")+'" maxlength="300" placeholder="https://ics.teamup.com/feed/…/0.ics" style="'+PROG_FELT+';margin:4px 0 4px">'+
        '<p style="color:var(--graa);font-size:.75rem;margin:0 0 10px">Findes i Teamup under Indstillinger → Del kalender → dit link → «iCalendar-feed». Uden feed vises «I dag»-listen ikke.</p>'+
        '<button id="kalGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem</button>'+
        '</div>'+
        (c.link?'<iframe id="kalFrame" src="'+fbaEsc(c.link)+'" title="Teamup-kalender" style="width:100%;height:74vh;border:0;border-radius:18px;background:#fff;box-shadow:var(--skygge)"></iframe>'+
            '<p style="color:var(--graa);font-size:.78rem;margin-top:8px">Er vinduet tomt, blokerer kalender-linket for indlejring — brug da «Åbn i egen fane» og sig til, så kigger vi på linket.</p>':
            '<p style="color:var(--graa)">Der er ikke sat et kalender-link op endnu.</p>');
    el.innerHTML=h;
    const nf=document.getElementById("kalNyFane");
    if(nf)nf.onclick=()=>{if(window.HFVin&&window.HFVin.aabn)window.HFVin.aabn("hf-prog-teamup",c.link,"","Teamup");else window.open(c.link,"_blank");};
    const ob=document.getElementById("kalOpsBtn"),ops=document.getElementById("kalOps");
    if(ob&&ops)ob.onclick=()=>{ops.style.display=ops.style.display==="none"?"":"none";};
    const gm=document.getElementById("kalGem");
    if(gm)gm.onclick=async function(){
        this.disabled=true;
        const sess=fbaSess();
        try{const r=await apiKald({a:"teamupgem",token:sess.token,link:document.getElementById("kalLink").value.trim(),ics:document.getElementById("kalIcs").value.trim()});
            if(r&&r.ok){kalConf=null;idagCache=null;renderKalender();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
    };
}
/* 👋 Til stede-tavlen + 🎂 mærkedage på Min dag (data fra lcadmin/lcaData) */
const TILST_VIS={kontor:["🏢","På kontoret"],hjemme:["🏠","Hjemme"],moede:["📞","Optaget"],ferie:["🌴","Ferie/fri"],syg:["🤒","Syg"]};
var tsAndreVis=false;
function renderTilstede(){
    const el=document.getElementById("admTilstede");if(!el||admOmr!=="dash")return;
    const d=lcaData||{};
    const ret=d.ret||{};
    const md=(d.maerkedage||[]).map(e=>e.type==="foedselsdag"?'🎂 <b>'+fbaEsc(e.vis)+'</b> har fødselsdag i dag!':'🏅 <b>'+fbaEsc(e.vis)+'</b> har '+e.aar+'-års jubilæum i dag!');
    const ts=d.tilstede||{};
    const grupper=Object.keys(TILST_VIS).map(kode=>{
        const navne=Object.keys(ts).filter(l=>ts[l]&&ts[l].kode===kode).map(l=>fbaEsc(visA(l)));
        return navne.length?'<span style="margin-right:14px;white-space:normal">'+TILST_VIS[kode][0]+' <b>'+TILST_VIS[kode][1]+':</b> '+navne.join(", ")+'</span>':"";
    }).join("");
    const knapper=(ret.statusandre?'<div style="margin-top:4px">'+
        '<button id="tsAndre" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.72rem;font-weight:700">'+(tsAndreVis?"✕ Luk":"✏️ Sæt status for kollega")+'</button>'+
        '</div>':'')+'<div id="tsAndreBoks"></div>';
    if(!md.length&&!grupper){
        el.innerHTML='<div style="background:#fff;border-radius:16px;padding:12px 18px;box-shadow:var(--skygge);font-size:.85rem;color:var(--graa)">👋 Ingen har sat status endnu — brug 👋 i admin-linjen.'+knapper+'</div>';
    }else{
        el.innerHTML='<div style="background:#fff;border-radius:16px;padding:12px 18px;box-shadow:var(--skygge);font-size:.88rem">'+
            (md.length?'<div style="margin-bottom:'+(grupper?'8px':'0')+'">'+md.join("<br>")+'</div>':"")+
            (grupper?'<div style="color:var(--moerk)">'+grupper+'</div>':"")+
            knapper+'</div>';
    }
    const ta=document.getElementById("tsAndre");
    if(ta)ta.onclick=()=>{tsAndreVis=!tsAndreVis;renderTilstede();};
    if(tsAndreVis&&ret.statusandre)tsAndreTegn(ts);
    /* ✏️ status for kolleger (perm 'statusandre'): søg → klik navn → klik status */
    function tsAndreTegn(ts){
        const boks=document.getElementById("tsAndreBoks");if(!boks)return;
        boks.innerHTML='<div style="border-top:1px solid rgba(30,55,76,.1);margin-top:8px;padding-top:8px">'+
            '<input id="tsSoeg" placeholder="🔎 Søg kollega … (fx da)" style="'+PROG_FELT+';max-width:260px;margin:0 0 6px">'+
            '<div id="tsListe" style="display:flex;gap:6px;flex-wrap:wrap"></div>'+
            '<div id="tsValg" style="margin-top:6px"></div></div>';
        const soegF=document.getElementById("tsSoeg"),listeEl=document.getElementById("tsListe"),valgEl=document.getElementById("tsValg");
        let valgtL=null;
        function tegn(){
            const soeg=(soegF.value||"").toLowerCase().trim();
            const alle=((lcaData&&lcaData.admins)||[]).filter(l=>l!==(fbaSess()||{}).u)
                .filter(l=>!soeg||visA(l).toLowerCase().indexOf(soeg)>=0||l.toLowerCase().indexOf(soeg)>=0).slice(0,20);
            listeEl.innerHTML=alle.map(l=>'<button class="ts-k" data-l="'+fbaEsc(l)+'" style="'+PROG_KNAP+(valgtL===l?';background:var(--moerk);color:#fff;border:0':'')+'">'+
                (ts[l]&&TILST_VIS[ts[l].kode]?TILST_VIS[ts[l].kode][0]+" ":"")+fbaEsc(visA(l))+'</button>').join("")||'<span style="font-size:.78rem;color:var(--graa)">ingen fundet</span>';
            listeEl.querySelectorAll(".ts-k").forEach(k=>{k.onclick=()=>{valgtL=k.getAttribute("data-l");tegn();};});
            valgEl.innerHTML=valgtL?'<b style="font-size:.8rem">'+fbaEsc(visA(valgtL))+' sættes til:</b> '+
                Object.keys(TILST_VIS).map(kode=>'<button class="ts-s" data-k="'+kode+'" style="'+PROG_KNAP+'">'+TILST_VIS[kode][0]+' '+TILST_VIS[kode][1]+'</button>').join(" ")+
                ' <button class="ts-s" data-k="" style="'+PROG_KNAP+';color:var(--roed)">✕ Ryd</button>':"";
            valgEl.querySelectorAll(".ts-s").forEach(k=>{k.onclick=async()=>{
                const sess=fbaSess();
                try{const r=await apiKald({a:"statussaet",token:sess.token,login:valgtL,kode:k.getAttribute("data-k")});
                    if(r&&r.ok){
                        if(!lcaData.tilstede)lcaData.tilstede={};
                        if(k.getAttribute("data-k"))lcaData.tilstede[valgtL]={kode:k.getAttribute("data-k")};
                        else delete lcaData.tilstede[valgtL];
                        renderTilstede();
                    }else alert((r&&r.fejl)||"Kunne ikke gemme");}catch(e){}
            };});
        }
        soegF.addEventListener("input",tegn);
        tegn();
    }
}
/* 🌟 Kudos — ros-strømmen på Min dag + giv ros */
var kudosData=null,kudosGivVis=false;
async function renderKudos(){
    const el=document.getElementById("admKudos");if(!el||admOmr!=="dash")return;
    const sess=fbaSess();if(!sess)return;
    if(!kudosData){
        try{const j=await apiKald({a:"kudosliste",token:sess.token});if(j&&j.ok)kudosData=j;}catch(e){return;}
    }
    if(!kudosData||admOmr!=="dash")return;
    const d=kudosData;
    let h='<div style="background:#fff;border-radius:16px;padding:14px 18px;box-shadow:var(--skygge)">'+
        '<div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap"><b style="font-size:.92rem">🌟 Ros til kollegerne</b>'+
        (d.mine?'<span style="background:rgba(147,213,0,.2);color:#41630a;font-size:.72rem;font-weight:800;border-radius:999px;padding:2px 10px">du har fået '+d.mine+' 🌟</span>':'')+
        '<span style="flex:1"></span>'+
        '<button id="kudosGiv" style="'+PROG_KNAP+';background:linear-gradient(120deg,#e7a600,#f5c93d);color:#4a3500;border:0">'+(kudosGivVis?"✕ Luk":"🌟 Giv ros")+'</button></div>'+
        '<div id="kudosPanel" style="display:'+(kudosGivVis?"block":"none")+';margin-top:10px;border-top:1px solid rgba(30,55,76,.08);padding-top:10px">'+
        '<input id="kuSoeg" placeholder="🔎 Hvem skal have ros? (søg — fx da)" style="'+PROG_FELT+';max-width:280px;margin:0 0 6px">'+
        '<div id="kuHits" style="display:flex;gap:6px;flex-wrap:wrap;margin:0 0 8px"></div>'+
        '<div id="kuValgt" style="font-size:.85rem;font-weight:800;margin:0 0 6px"></div>'+
        '<textarea id="kuTxt" rows="2" maxlength="300" placeholder="Hvorfor fortjener de ros? (vises for alle i den officielle kanal)" style="'+PROG_FELT+';resize:vertical"></textarea>'+
        '<button id="kuSend" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12;margin-top:6px">Send rosen 🌟</button></div>'+
        ((d.seneste||[]).length?'<div style="margin-top:8px">'+
            d.seneste.slice(0,4).map(k=>'<div style="display:flex;gap:8px;padding:5px 0;border-top:1px solid rgba(30,55,76,.05);font-size:.85rem;align-items:baseline">'+
                '<span style="flex:none">🌟</span><span><b>'+fbaEsc(k.tilVis)+'</b> <span style="color:var(--graa)">fik ros af '+fbaEsc(k.fraVis)+' · '+fbaDato(k.ts)+'</span><br>«'+fbaEsc(k.txt)+'»</span>'+
                (d.retSlet?'<button class="ku-slet" data-id="'+fbaEsc(k.id)+'" title="Fjern" style="margin-left:auto;border:0;background:none;cursor:pointer;color:var(--graa)">✕</button>':'')+
                '</div>').join("")+'</div>'
            :'<p style="color:var(--graa);font-size:.82rem;margin:8px 0 0">Ingen ros endnu — vær den første! 🌟</p>')+
        '</div>';
    el.innerHTML=h;
    let kuTil=null;
    const gv=document.getElementById("kudosGiv");
    if(gv)gv.onclick=()=>{kudosGivVis=!kudosGivVis;renderKudos();};
    const soeg=document.getElementById("kuSoeg"),hits=document.getElementById("kuHits"),valgtEl=document.getElementById("kuValgt");
    function kuTegn(){
        const q=(soeg.value||"").toLowerCase().trim();
        const alle=((lcaData&&lcaData.admins)||[]).filter(l=>l!==(fbaSess()||{}).u)
            .filter(l=>!q||visA(l).toLowerCase().indexOf(q)>=0||String(l).toLowerCase().indexOf(q)>=0).slice(0,10);
        hits.innerHTML=alle.map(l=>'<button class="ku-k" data-l="'+fbaEsc(l)+'" style="'+PROG_KNAP+(kuTil===l?';background:var(--moerk);color:#fff;border:0':'')+'">'+fbaEsc(visA(l))+'</button>').join("");
        hits.querySelectorAll(".ku-k").forEach(k=>{k.onclick=()=>{kuTil=k.getAttribute("data-l");valgtEl.textContent="🌟 Ros til: "+visA(kuTil);kuTegn();};});
    }
    if(soeg){soeg.addEventListener("input",kuTegn);if(kudosGivVis)kuTegn();}
    const send=document.getElementById("kuSend");
    if(send)send.onclick=async function(){
        const txt=(document.getElementById("kuTxt").value||"").trim();
        if(!kuTil){alert("Vælg en kollega først.");return;}
        if(txt.length<3){alert("Skriv et par ord om hvorfor.");return;}
        this.disabled=true;
        try{const r=await apiKald({a:"kudosgiv",token:sess.token,til:kuTil,txt:txt});
            if(r&&r.ok){kudosData=null;kudosGivVis=false;renderKudos();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}
        catch(e){this.disabled=false;}
    };
    el.querySelectorAll(".ku-slet").forEach(k=>{k.onclick=async()=>{
        if(!confirm("Fjern denne ros fra strømmen?"))return;
        try{const r=await apiKald({a:"kudosslet",token:sess.token,id:k.getAttribute("data-id")});
            if(r&&r.ok){kudosData=null;renderKudos();}}catch(e){}
    };});
}
/* 📅 I dag-kortet på Min dag (10 min server-cache; klient-cache pr. side-load) */
var idagCache=null;
async function renderIdag(){
    const el=document.getElementById("admIdag");if(!el||admOmr!=="dash")return;
    const sess=fbaSess();if(!sess)return;
    let j=idagCache;
    if(!j){try{j=await apiKald({a:"teamupidag",token:sess.token});idagCache=j;}catch(e){return;}}
    if(!j||!j.ok||admOmr!=="dash")return;
    if(j.mangler){
        el.innerHTML='<div style="background:#fff;border-radius:16px;padding:14px 18px;box-shadow:var(--skygge);color:var(--graa);font-size:.85rem">📅 <b>I dag i kalenderen</b> — tilføj iCalendar-feedet under <a href="#" id="idagTilKal" style="color:var(--blaa)">🗓 Kalender</a>, så vises dagens aftaler her.</div>';
        const a=document.getElementById("idagTilKal");
        if(a)a.onclick=e=>{e.preventDefault();visOmraade("kal");};
        return;
    }
    const rk=(j.aftaler||[]).map(x=>'<div style="display:flex;gap:10px;padding:3px 0;font-size:.88rem">'+
        '<span style="font-weight:800;color:var(--blaa);min-width:44px">'+(x.tid||"hele dagen")+'</span><span>'+fbaEsc(x.titel)+'</span></div>').join("");
    el.innerHTML='<div style="background:#fff;border-radius:16px;padding:14px 18px;box-shadow:var(--skygge)">'+
        '<b style="font-size:.92rem">📅 I dag i kalenderen</b>'+(j.gammel?' <span style="color:var(--graa);font-size:.72rem">(fra sidste hentning)</span>':'')+
        '<div style="margin-top:6px">'+(rk||'<span style="color:var(--graa);font-size:.85rem">Ingen aftaler i dag 🎉</span>')+'</div></div>';
}
/* ===================== 📌 Notetavlen — digitale post-it-sedler ===================== */
var notData=null,notRedig=null,notVisArkiv=false,notKatFilter=null,notKatPanel=false,notPaaPanel=false;   /* notRedig: nid eller "ny" */
const NOT_VARSEL_VALG=[[1,"1 time før"],[3,"3 timer før"],[6,"6 timer før"],[12,"12 timer før"],[24,"1 dag før"],[48,"2 dage før"],[72,"3 dage før"],[168,"1 uge før"]];
function notVarselTekst(t){for(var i=0;i<NOT_VARSEL_VALG.length;i++)if(NOT_VARSEL_VALG[i][0]===t)return NOT_VARSEL_VALG[i][1];return t+" timer før";}
var notGrp="kat";try{notGrp=localStorage.getItem("not_grp")||"kat";}catch(e){}   /* kat | farve | ny */
const NOT_HEX={gul:"#fff3b0",groen:"#d8f5d0",blaa:"#d6ecff",pink:"#ffdbe7",lilla:"#e8ddff",orange:"#ffe3c4"};
const NOT_FARVENAVN={gul:"Gul",groen:"Grøn",blaa:"Blå",pink:"Pink",lilla:"Lilla",orange:"Orange"};
function notKatNavn(id){const k=((notData&&notData.kategorier)||[]).find(x=>x.id===id);return k?k.navn:null;}
/* redigeres en seddel oppe i notesektionen, opdaterer tavlen sig selv */
window.addEventListener("hfNoter",async ev=>{
    notFristCache=null;   /* en frist kan være ændret → tving frisk «⏰ Frister» i Dagens overblik */
    if(admOmr==="dash"){try{renderDigest();}catch(e){}}   /* står man på Overblik, opdatér striben straks (fx efter hurtig-seddel) */
    if(ev&&ev.detail&&ev.detail.fra==="tavle")return;
    if(admOmr!=="noter")return;
    await notHent(true);renderNoter();
});
async function notHent(frisk){
    if(notData&&!frisk)return notData;
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"noter",token:sess.token});
        if(j&&j.ok){notData=j;return notData;}}catch(e){}
    return null;
}
const NOT_MDR=["jan","feb","mar","apr","maj","jun","jul","aug","sep","okt","nov","dec"];
function notFristPille(n){   /* minimalistisk deadline-mærkat — kun hvis frist sat; farve efter resttid */
    if(!n||!n.fristTs)return "";
    var nu=Date.now()/1000, rest=n.fristTs-nu, harTid=!!n.fristKl;
    var d=new Date(n.fristTs*1000), na=new Date();
    var sammeDag=d.toDateString()===na.toDateString();
    var iMorgen=new Date(na.getTime()+864e5).toDateString()===d.toDateString();
    var iGaar=new Date(na.getTime()-864e5).toDateString()===d.toDateString();
    var kl=harTid?(("0"+d.getHours()).slice(-2)+":"+("0"+d.getMinutes()).slice(-2)):"";
    var dato=d.getDate()+". "+NOT_MDR[d.getMonth()]+(d.getFullYear()!==na.getFullYear()?" "+d.getFullYear():"");
    var bg,fg,ikon,txt;
    if(rest<0){ bg="#FCEBEB";fg="#A32D2D";ikon="⚠";
        txt=iGaar?"forfaldt i går":(sammeDag?"forfaldt i dag":"forfaldt "+dato); }
    else if(sammeDag){ bg="#FAECE7";fg="#993C1D";ikon="⏰"; txt=harTid?("i dag · "+kl):"i dag"; }
    else if(iMorgen){ bg=n.haster?"#FAEEDA":"#F1EFE8";fg=n.haster?"#854F0B":"#5F5E5A";ikon="⏰"; txt=harTid?("i morgen · "+kl):"i morgen"; }
    else { var dage=Math.ceil(rest/86400);
        if(n.haster){bg="#FAEEDA";fg="#854F0B";ikon="⏰";}else{bg="#EFEBE0";fg="#5F5E5A";ikon="⏳";}
        txt=(dage<=7?("om "+dage+" dage"):dato); }
    return '<span title="Deadline: '+fbaEsc(dato+(kl?" kl. "+kl:""))+'" style="display:inline-flex;align-items:center;gap:4px;font-size:.68rem;font-weight:800;padding:2px 8px;border-radius:999px;background:'+bg+';color:'+fg+'">'+ikon+' '+fbaEsc(txt)+'</span>';
}
var notFristCache=null,notFristCacheTs=0;
async function notFristAktive(force){   /* delt kilde: aktive frist-noter (haster/forfaldt). notefristtjek fyrer også PWA-push server-side (idempotent). Cache 45s. */
    if(!force&&notFristCache&&Date.now()-notFristCacheTs<45000)return notFristCache;
    var sess=(typeof fbaSess==="function")?fbaSess():null;
    if(!sess||!sess.token)return notFristCache||[];
    try{var r=await apiKald({a:"notefristtjek",token:sess.token});if(r&&r.ok){notFristCache=r.aktive||[];notFristCacheTs=Date.now();}}catch(e){}
    return notFristCache||[];
}
function notFristChip(n){   /* aflang chip til Dagens overblik-rækken — farve/vægt eskalerer med hvor tæt fristen er */
    var nu=Date.now()/1000, rest=n.fristTs-nu;
    var d=new Date(n.fristTs*1000), na=new Date();
    var harTid=!!n.fristKl, kl=harTid?(("0"+d.getHours()).slice(-2)+":"+("0"+d.getMinutes()).slice(-2)):"";
    var bg,fg,w,naar,puls=0;
    if(rest<0){bg="rgba(231,0,76,.14)";fg="#A32D2D";w=900;naar="forfaldt";puls=1;}
    else if(d.toDateString()===na.toDateString()){bg="#FBE4D8";fg="#993C1D";w=800;naar="i dag"+(harTid?" "+kl:"");}
    else{var dage=Math.ceil(rest/86400);bg="#FBF1D6";fg="#7a4d08";w=600;naar=(dage<=1)?"i morgen":("om "+dage+" dage");}
    var titel=(n.txt||"Note").replace(/\s+/g," ").trim();
    if(titel.length>26)titel=titel.slice(0,25)+"…";
    return '<span class="fba-side notfrist-chip'+(puls?" notfrist-puls":"")+'" data-id="'+fbaEsc(n.id)+'" title="'+fbaEsc(n.txt||"")+' — åbn på din notetavle" style="cursor:pointer;font-size:.83rem;font-weight:'+w+';background:'+bg+';color:'+fg+'">⏰ '+fbaEsc(titel)+' · '+naar+'</span>';
}
function notFristSamlet(liste){   /* 1 frist → vis noten; flere → én samlet chip m. lille antals-badge */
    if(!liste||!liste.length)return "";
    if(liste.length===1)return notFristChip(liste[0]);
    var nu=Date.now()/1000, rang=0, bg="#FBF1D6", fg="#7a4d08", puls=0;
    liste.forEach(function(n){
        var rest=n.fristTs-nu, r,_bg,_fg,_p=0;
        if(rest<0){r=3;_bg="rgba(231,0,76,.14)";_fg="#A32D2D";_p=1;}
        else{var d=new Date(n.fristTs*1000),na=new Date();if(d.toDateString()===na.toDateString()){r=2;_bg="#FBE4D8";_fg="#993C1D";}else{r=1;_bg="#FBF1D6";_fg="#7a4d08";}}
        if(r>rang){rang=r;bg=_bg;fg=_fg;puls=_p;}
    });
    var txt=(rang>=3)?" noter kræver din opmærksomhed":" noter nærmer sig fristen";
    return '<span class="fba-side notfrist-chip'+(puls?" notfrist-puls":"")+'" data-id="__flere" title="Se alle dine frister på notetavlen" style="cursor:pointer;font-size:.83rem;font-weight:800;background:'+bg+';color:'+fg+'">⏰ <span style="display:inline-flex;align-items:center;justify-content:center;min-width:20px;height:20px;border-radius:999px;background:'+fg+';color:#fff;font-size:.74rem;font-weight:900;padding:0 6px;vertical-align:-2px">'+liste.length+'</span>'+txt+'</span>';
}
async function notFristPaamind(){   /* viser popup for aktive deadlines der ikke er kvitteret i dag (deler kilde m. Dagens overblik) */
    var aktive=await notFristAktive();
    if(!aktive.length)return;
    var idag=new Date().toISOString().slice(0,10);
    var vis=aktive.filter(function(n){try{return localStorage.getItem("notpop_"+n.id+"_"+n.niv)!==idag;}catch(e){return true;}});
    if(vis.length)notFristPopupVis(vis,idag);
}
function notFristPopupVis(liste,idag){
    if(document.getElementById("notPopOv"))return;
    var forfaldt=liste.filter(function(n){return n.niv===2;}).length;
    var ov=document.createElement("div");ov.id="notPopOv";
    ov.style.cssText="position:fixed;inset:0;z-index:9996;background:rgba(30,55,76,.45);display:flex;align-items:center;justify-content:center;padding:20px";
    ov.innerHTML='<div style="width:min(440px,100%);max-height:82vh;display:flex;flex-direction:column;background:#fff;border-radius:20px;box-shadow:0 30px 80px rgba(0,0,0,.4);overflow:hidden;font-family:var(--font)">'+
        '<div style="padding:15px 20px;background:'+(forfaldt?"#e7004c":"#f5a623")+';color:#fff"><b style="font-size:1.02rem">⏰ Deadline-påmindelse</b><div style="font-size:.8rem;opacity:.92">'+liste.length+' note'+(liste.length>1?"r":"")+' kræver din opmærksomhed</div></div>'+
        '<div style="padding:14px 18px;overflow-y:auto;display:flex;flex-direction:column;gap:10px">'+
        liste.map(function(n){return '<div style="background:'+(NOT_HEX[n.farve]||NOT_HEX.gul)+';border-radius:12px;padding:11px 13px">'+
            '<div style="font-size:.9rem;color:#2a3a48;white-space:pre-wrap;word-break:break-word">'+fbaEsc(n.txt)+'</div>'+
            '<div style="margin-top:7px">'+notFristPille({fristTs:n.fristTs,fristKl:n.fristKl,haster:1})+'</div></div>';}).join("")+
        '</div>'+
        '<div style="padding:12px 18px;display:flex;gap:8px;align-items:center;border-top:1px solid rgba(30,55,76,.1)">'+
        '<button id="notPopTavle" style="'+PROG_KNAP+';background:var(--moerk);color:#fff;border:0">📌 Åbn tavlen</button>'+
        '<span style="flex:1"></span>'+
        '<button id="notPopLuk" style="'+PROG_KNAP+'">Set — luk</button></div></div>';
    document.body.appendChild(ov);
    var kvit=function(){try{liste.forEach(function(n){localStorage.setItem("notpop_"+n.id+"_"+n.niv,idag);});}catch(e){}};
    ov.querySelector("#notPopLuk").onclick=function(){kvit();ov.remove();};
    ov.querySelector("#notPopTavle").onclick=function(){kvit();ov.remove();try{visOmraade("noter");}catch(e){}};
    ov.addEventListener("click",function(e){if(e.target===ov){kvit();ov.remove();}});
}
function notSeddelHTML(n,egen){
    const hex=NOT_HEX[n.farve]||NOT_HEX.gul;
    const tidTxt=(typeof fsTid==="function")?fsTid(n.aendret||n.ts):"";
    if(notRedig===n.id&&egen)return notRedigHTML(n);
    const delt=egen&&(n.til||[]).length?'<span title="'+fbaEsc((n.til||[]).map(t=>visA(t)).join(", "))+'">🤝 delt med '+(n.til||[]).length+'</span>':'';
    return '<div style="break-inside:avoid;margin:0 0 12px;background:'+hex+';border-radius:14px;padding:14px 16px;'+
        'box-shadow:0 6px 18px rgba(30,55,76,.14);position:relative">'+
        (n.pin?'<span style="position:absolute;top:-7px;left:14px;font-size:1.05rem" title="Fastgjort — vises i notesektionen på alle sider">📌</span>':'')+
        (n.top?'<span style="position:absolute;top:-7px;right:14px;background:#1e374c;color:#fff;font-size:.62rem;font-weight:800;border-radius:999px;padding:2px 8px" title="Topnote — ligger altid øverst">📍 TOP</span>':'')+
        '<div style="white-space:pre-wrap;font-size:.92rem;color:#2a3a48;word-break:break-word">'+fbaEsc(n.txt)+'</div>'+
        '<div style="display:flex;gap:8px;align-items:center;margin-top:10px;font-size:.72rem;color:rgba(42,58,72,.65);flex-wrap:wrap">'+
        notFristPille(n)+
        (egen?'':'<b>'+fbaEsc(n.afVis||n.af)+'</b> · ')+tidTxt+(delt?' · '+delt:'')+
        (egen&&n.kat&&notKatNavn(n.kat)?' · <span>🏷 '+fbaEsc(notKatNavn(n.kat))+'</span>':'')+
        '<span style="flex:1"></span>'+
        (egen?'<button class="not-k" data-h="redig" data-id="'+n.id+'" title="Redigér">✏️</button>'+
            '<button class="not-k" data-h="pin" data-id="'+n.id+'" title="'+(n.pin?"Frigør — fjernes fra notesektionen":"Fastgør — vises i notesektionen på alle sider")+'">'+(n.pin?"📌✕":"📌")+'</button>'+
            '<button class="not-k" data-h="top" data-id="'+n.id+'" title="'+(n.top?"Fjern som topnote":"Gør til topnote — ligger altid øverst (kun én ad gangen)")+'">'+(n.top?"📍✕":"📍")+'</button>'+
            '<button class="not-k" data-h="arkiv" data-id="'+n.id+'" title="Arkivér">🗄</button>'
            :'<button class="not-k" data-h="afvis" data-id="'+n.id+'" title="Fjern fra min tavle">✕</button>')+
        '</div></div>';
}
window.notKolFilter=function(inp){   /* indsnævrer kollega-listen — matcher hvor som helst i navnet */
    var q=(inp.value||"").trim().toLowerCase();
    var liste=inp.parentNode.querySelector(".nr-kol-liste");if(!liste)return;
    liste.querySelectorAll("label[data-navn]").forEach(function(l){l.style.display=(!q||l.getAttribute("data-navn").indexOf(q)>=0)?"":"none";});
};
function notRedigHTML(n){
    const hex=NOT_HEX[n.farve]||NOT_HEX.gul;
    const admins=(notData&&notData.admins)||[];
    const _nt=String(n.txt||"");
    const _rows=Math.min(18,Math.max(4,(_nt.match(/\n/g)||[]).length+1+Math.floor(_nt.length/38)));   /* behold notens størrelse ved redigering */
    return '<div class="not-redig" data-id="'+(n.id||"")+'" style="break-inside:avoid;margin:0 0 12px;background:'+hex+';border-radius:14px;padding:14px 16px;box-shadow:0 10px 26px rgba(30,55,76,.22)">'+
        '<textarea class="nr-txt" rows="'+_rows+'" maxlength="2000" placeholder="Skriv din seddel …" style="width:100%;box-sizing:border-box;border:0;background:rgba(255,255,255,.5);border-radius:9px;padding:9px 11px;font-family:var(--font);font-size:.92rem;resize:vertical">'+fbaEsc(n.txt||"")+'</textarea>'+
        '<div style="display:flex;gap:6px;margin:9px 0" class="nr-farver">'+
        Object.keys(NOT_HEX).map(f=>'<button class="nr-farve" data-f="'+f+'" title="'+f+'" style="width:24px;height:24px;border-radius:50%;cursor:pointer;background:'+NOT_HEX[f]+';border:2px solid '+(f===(n.farve||"gul")?"#1e374c":"rgba(30,55,76,.15)")+'"></button>').join("")+
        '<span style="flex:1"></span>'+
        '<label style="font-size:.75rem;color:#2a3a48;display:flex;align-items:center;gap:4px"><input type="checkbox" class="nr-pin"'+(n.pin?" checked":"")+'> 📌 fastgør</label></div>'+
        '<select class="nr-kat" style="width:100%;box-sizing:border-box;border:0;background:rgba(255,255,255,.5);border-radius:9px;padding:7px 9px;font-family:var(--font);font-size:.8rem;margin:0 0 9px">'+
        '<option value="">🏷 Uden kategori</option>'+
        ((notData&&notData.kategorier)||[]).map(k=>'<option value="'+fbaEsc(k.id)+'"'+(n.kat===k.id?" selected":"")+'>'+fbaEsc(k.navn)+'</option>').join("")+
        '</select>'+
        '<div style="display:flex;gap:6px;align-items:center;margin:0 0 9px;flex-wrap:wrap">'+
        '<span style="font-size:.75rem;font-weight:800;color:#2a3a48">⏰ Deadline</span>'+
        '<input type="date" class="nr-frist" value="'+fbaEsc(n.frist||"")+'" style="border:0;background:rgba(255,255,255,.6);border-radius:8px;padding:5px 8px;font-family:var(--font);font-size:.8rem">'+
        '<input type="time" class="nr-fristkl" value="'+fbaEsc(n.fristKl||"")+'" title="Valgfrit klokkeslæt" style="border:0;background:rgba(255,255,255,.6);border-radius:8px;padding:5px 8px;font-family:var(--font);font-size:.8rem">'+
        '<button type="button" class="nr-frist-ryd not-k" title="Fjern deadline" style="padding:3px 8px">✕</button>'+
        '</div>'+
        '<details'+((n.til||[]).length?' open':'')+' style="margin:0 0 9px"><summary style="cursor:pointer;font-size:.78rem;font-weight:800;color:#2a3a48">🤝 Del med kolleger'+((n.til||[]).length?' ('+(n.til||[]).length+')':'')+'</summary>'+
        (admins.length>6?'<input class="nr-kol-soeg" oninput="notKolFilter(this)" placeholder="🔍 Søg kollega …" style="width:100%;box-sizing:border-box;border:0;background:rgba(255,255,255,.65);border-radius:8px;padding:6px 10px;font-family:var(--font);font-size:.8rem;margin:6px 0 4px">':'')+
        '<div class="nr-tg" style="display:flex;flex-wrap:wrap;gap:5px;margin:2px 0 0;align-items:center"></div>'+
        '<div class="nr-kol-liste" style="max-height:150px;overflow:auto;margin-top:6px;background:rgba(255,255,255,.5);border-radius:9px;padding:8px">'+
        (admins.length?admins.map(x=>'<label data-navn="'+fbaEsc((x.vis||x.login||"").toLowerCase())+'" style="display:flex;gap:6px;align-items:center;font-size:.8rem;padding:2px 0"><input type="checkbox" class="nr-til" value="'+fbaEsc(x.login)+'"'+((n.til||[]).indexOf(x.login)>=0?" checked":"")+'> '+fbaEsc(x.vis)+'</label>').join(""):'<span style="font-size:.78rem;color:rgba(42,58,72,.6)">ingen kolleger fundet</span>')+
        '</div></details>'+
        '<div style="display:flex;gap:8px">'+
        '<button class="not-k nr-gem" style="font-weight:800">💾 Gem</button>'+
        '<button class="not-k nr-fortryd">Fortryd</button>'+
        (n.id?'<span style="flex:1"></span><button class="not-k nr-slet" data-id="'+n.id+'" style="color:var(--roed)">🗑 Slet</button>':'')+
        '</div></div>';
}
async function renderNoter(){
    const el=document.getElementById("notWrap");if(!el)return;
    if(!notData)el.innerHTML='<p style="color:var(--graa)">⏳ Henter din tavle …</p>';
    const d=await notHent();
    if(admOmr!=="noter")return;
    if(!d){el.innerHTML='<p style="color:var(--graa)">Kunne ikke hente notetavlen.</p>';return;}
    const kol='display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:12px;align-items:start';   /* grid (låst rækkefølge) i st.f. kolonne-masonry: en note bliver liggende når den redigeres — kun noterne under skubbes ned, ingen kolonne-hop */
    const CHIP=(aktiv)=>'border:1px solid '+(aktiv?'var(--moerk)':'rgba(30,55,76,.16)')+';background:'+(aktiv?'var(--moerk)':'#f4f8fb')+';color:'+(aktiv?'#fff':'var(--moerk)')+';border-radius:999px;cursor:pointer;font-family:var(--font);font-size:.76rem;font-weight:800;padding:5px 12px';
    const kats=(d.kategorier||[]);
    const antal=id=>(d.mine||[]).filter(n=>id===null?true:(id===""?!(n.kat&&notKatNavn(n.kat)):n.kat===id)).length;
    let h='<p class="intro" style="margin-top:4px">Din digitale post-it-tavle. Sedlerne er <b>dine egne</b> — del en seddel med kolleger, så lander den også på deres tavle. 📌 fastgjorte vises i notesektionen på alle sider, 📍 topnoten ligger altid øverst.</p>'+
        '<div style="margin:0 0 10px;display:flex;gap:8px;flex-wrap:wrap;align-items:center">'+
        '<button id="notNy" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">➕ Ny seddel</button>'+
        '<button id="notArkivBtn" style="'+PROG_KNAP+'">'+(notVisArkiv?"‹ Tilbage til tavlen":"🗄 Arkiv ("+(d.arkiv||[]).length+")")+'</button>'+
        (notVisArkiv?'':'<span style="flex:1"></span>'+
            '<span style="font-size:.74rem;font-weight:800;color:var(--graa)">Gruppér:</span>'+
            [["kat","📂 Kategori"],["farve","🎨 Farve"],["ny","🕐 Nyeste"]].map(g=>'<button class="not-grp" data-g="'+g[0]+'" style="'+CHIP(notGrp===g[0])+'">'+g[1]+'</button>').join(""))+
        '</div>'+
        (notVisArkiv?'':'<div style="margin:0 0 14px;display:flex;gap:6px;flex-wrap:wrap;align-items:center">'+
            '<button class="not-kf" data-k="__alle" style="'+CHIP(notKatFilter===null)+'">Alle ('+antal(null)+')</button>'+
            kats.map(k=>'<button class="not-kf" data-k="'+fbaEsc(k.id)+'" style="'+CHIP(notKatFilter===k.id)+'">'+fbaEsc(k.navn)+' ('+antal(k.id)+')</button>').join("")+
            '<button class="not-kf" data-k="" style="'+CHIP(notKatFilter==="")+'">Uden kategori ('+antal("")+')</button>'+
            '<button id="notKatRedig" style="'+CHIP(notKatPanel)+'" title="Omdøb, tilføj eller fjern kategorier">✏️ Kategorier</button>'+
            '<button id="notPaaBtn" style="'+CHIP(notPaaPanel)+'" title="Hvornår skal deadline-noter varsle dig?">⏰ Påmindelser</button>'+
            '</div>');
    if(notKatPanel&&!notVisArkiv){
        h+='<div id="notKatBoks" style="background:#fff;border-radius:14px;padding:14px;box-shadow:var(--skygge);margin:0 0 14px;max-width:480px">'+
            '<b style="font-size:.85rem">✏️ Mine kategorier</b>'+
            '<p style="color:var(--graa);font-size:.75rem;margin:4px 0 10px">Skriv gerne et ikon (emoji) foran navnet. Slettes en kategori, ender dens sedler under «Uden kategori».</p>'+
            '<div id="notKatListe">'+
            kats.map(k=>'<div style="display:flex;gap:6px;margin:0 0 6px"><input class="nk-navn" data-id="'+fbaEsc(k.id)+'" value="'+fbaEsc(k.navn)+'" maxlength="30" style="'+PROG_FELT+'">'+
                '<button class="nk-slet" style="'+PROG_KNAP+';color:var(--roed)" title="Fjern">🗑</button></div>').join("")+
            '</div>'+
            '<div style="display:flex;gap:8px;margin-top:4px">'+
            '<button id="nkTilf" style="'+PROG_KNAP+'">➕ Ny kategori</button>'+
            '<button id="nkGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem</button>'+
            '</div></div>';
    }
    if(notPaaPanel&&!notVisArkiv){
        const _v=(d.varsel)||{effektiv:24,central:24,egen:null,maaCentral:0};
        const _optHTML=(valgt,medBrug)=>(medBrug?'<option value="-1"'+(valgt===null?" selected":"")+'>Brug fælles standard ('+notVarselTekst(_v.central)+')</option>':'')+
            NOT_VARSEL_VALG.map(o=>'<option value="'+o[0]+'"'+(valgt===o[0]?" selected":"")+'>'+o[1]+'</option>').join("");
        h+='<div id="notPaaBoks" style="background:#fff;border-radius:14px;padding:16px;box-shadow:var(--skygge);margin:0 0 14px;max-width:480px">'+
            '<b style="font-size:.85rem">⏰ Deadline-påmindelser</b>'+
            '<p style="color:var(--graa);font-size:.75rem;margin:4px 0 12px">Når en note med deadline kommer inden for din varslingstid, løftes den automatisk op på tavlen, popper frem som påmindelse og sender dig en notifikation.</p>'+
            '<label style="display:block;font-size:.78rem;font-weight:800;color:var(--moerk);margin:0 0 4px">Varsl mig</label>'+
            '<select id="notPaaEgen" style="'+PROG_FELT+';max-width:280px">'+_optHTML(_v.egen,true)+'</select>'+
            (_v.maaCentral?'<div style="margin-top:14px;padding-top:12px;border-top:1px solid rgba(30,55,76,.1)">'+
                '<label style="display:block;font-size:.78rem;font-weight:800;color:var(--moerk);margin:0 0 4px">🏢 Fælles standard (gælder alle, der ikke selv har valgt)</label>'+
                '<select id="notPaaCentral" style="'+PROG_FELT+';max-width:280px">'+_optHTML(_v.central,false)+'</select>'+
                '<p style="color:var(--graa);font-size:.72rem;margin:6px 0 0">Du har rettighed til at sætte den fælles standard i brugerstyringen.</p></div>':'')+
            '</div>';
    }
    if(notVisArkiv){
        h+=(d.arkiv||[]).length?'<div style="'+kol+'">'+(d.arkiv||[]).map(n=>
            '<div style="break-inside:avoid;margin:0 0 12px;background:#f2f5f7;border-radius:14px;padding:14px 16px;box-shadow:var(--skygge)">'+
            '<div style="white-space:pre-wrap;font-size:.88rem;color:var(--graa);word-break:break-word">'+fbaEsc(n.txt)+'</div>'+
            '<div style="display:flex;gap:8px;margin-top:10px;font-size:.72rem;color:var(--graa)"><span style="flex:1"></span>'+
            '<button class="not-k" data-h="gendan" data-id="'+n.id+'">↩ Gendan</button>'+
            '<button class="not-k nr-slet" data-id="'+n.id+'" style="color:var(--roed)">🗑 Slet</button></div></div>').join("")+'</div>'
            :'<p style="color:var(--graa)">Arkivet er tomt.</p>';
    }else{
        h+=notRedig==="ny"?'<div style="max-width:340px">'+notRedigHTML({txt:"",farve:"gul",til:[],kat:notKatFilter||""})+'</div>':'';
        /* filtrér + gruppér mine sedler */
        let mine=(d.mine||[]).filter(n=>notKatFilter===null?true:(notKatFilter===""?!(n.kat&&notKatNavn(n.kat)):n.kat===notKatFilter));
        const OVERSKRIFT=t=>'<h4 style="margin:16px 0 8px;font-size:.76rem;letter-spacing:.08em;text-transform:uppercase;color:var(--graa)">'+t+'</h4>';
        const GITTER=l=>'<div style="'+kol+'">'+l.map(n=>notSeddelHTML(n,true)).join("")+'</div>';
        if(!mine.length&&notRedig!=="ny"){
            h+='<p style="color:var(--graa)">'+((d.mine||[]).length?'Ingen sedler i denne kategori.':'Ingen sedler endnu — tryk ➕ Ny seddel, eller brug 📌 i admin-linjen hvor som helst i universet.')+'</p>';
        }else if(notGrp==="kat"&&notKatFilter===null){
            kats.forEach(k=>{const l=mine.filter(n=>n.kat===k.id);if(l.length)h+=OVERSKRIFT(fbaEsc(k.navn)+' · '+l.length)+GITTER(l);});
            const uden=mine.filter(n=>!(n.kat&&notKatNavn(n.kat)));
            if(uden.length)h+=OVERSKRIFT('Uden kategori · '+uden.length)+GITTER(uden);
        }else if(notGrp==="farve"&&notKatFilter===null){
            Object.keys(NOT_HEX).forEach(f=>{const l=mine.filter(n=>(n.farve||"gul")===f);
                if(l.length)h+=OVERSKRIFT('<span style="display:inline-block;width:11px;height:11px;border-radius:50%;background:'+NOT_HEX[f]+';border:1px solid rgba(30,55,76,.25);vertical-align:-1px"></span> '+NOT_FARVENAVN[f]+' · '+l.length)+GITTER(l);});
        }else{
            if(mine.length)h+=GITTER(mine);
        }
        if((d.delte||[]).length)h+='<h3 style="margin:18px 0 10px;font-size:1rem">🤝 Delt med dig</h3><div style="'+kol+'">'+(d.delte||[]).map(n=>notSeddelHTML(n,false)).join("")+'</div>';
    }
    el.innerHTML=h;
    document.getElementById("notNy").onclick=()=>{notRedig="ny";notVisArkiv=false;renderNoter();};
    document.getElementById("notArkivBtn").onclick=()=>{notVisArkiv=!notVisArkiv;notRedig=null;notKatPanel=false;renderNoter();};
    el.querySelectorAll(".not-grp").forEach(k=>{k.onclick=()=>{
        notGrp=k.getAttribute("data-g");try{localStorage.setItem("not_grp",notGrp);}catch(e){}renderNoter();
    };});
    el.querySelectorAll(".not-kf").forEach(k=>{k.onclick=()=>{
        const v=k.getAttribute("data-k");
        notKatFilter=v==="__alle"?null:v;
        renderNoter();
    };});
    const nkr=document.getElementById("notKatRedig");
    if(nkr)nkr.onclick=()=>{notKatPanel=!notKatPanel;notPaaPanel=false;renderNoter();};
    const npb=document.getElementById("notPaaBtn");
    if(npb)npb.onclick=()=>{notPaaPanel=!notPaaPanel;notKatPanel=false;renderNoter();};
    const npEgen=document.getElementById("notPaaEgen");
    if(npEgen)npEgen.onchange=async function(){
        const sess=fbaSess();const val=this.value;this.disabled=true;
        try{const r=await apiKald({a:"notevarselsaet",token:sess.token,timer:val});
            if(r&&r.ok){await notHent(true);renderNoter();}else{this.disabled=false;}}
        catch(e){this.disabled=false;}
    };
    const npCentral=document.getElementById("notPaaCentral");
    if(npCentral)npCentral.onchange=async function(){
        const sess=fbaSess();const val=this.value;this.disabled=true;
        try{const r=await apiKald({a:"notecentralsaet",token:sess.token,timer:val});
            if(r&&r.ok){await notHent(true);renderNoter();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
    const nkBoks=document.getElementById("notKatBoks");
    if(nkBoks){
        const raek=(id,navn)=>{
            const r=document.createElement("div");
            r.style.cssText="display:flex;gap:6px;margin:0 0 6px";
            r.innerHTML='<input class="nk-navn" data-id="'+fbaEsc(id)+'" value="'+fbaEsc(navn)+'" maxlength="30" style="'+PROG_FELT+'">'+
                '<button class="nk-slet" style="'+PROG_KNAP+';color:var(--roed)" title="Fjern">🗑</button>';
            r.querySelector(".nk-slet").onclick=()=>r.remove();
            return r;
        };
        nkBoks.querySelectorAll(".nk-slet").forEach(k=>{k.onclick=()=>k.parentNode.remove();});
        document.getElementById("nkTilf").onclick=()=>{document.getElementById("notKatListe").appendChild(raek("",""));};
        document.getElementById("nkGem").onclick=async function(){
            const liste=[];
            nkBoks.querySelectorAll(".nk-navn").forEach(i=>{const navn=i.value.trim();if(navn)liste.push({id:i.getAttribute("data-id")||"",navn:navn});});
            this.disabled=true;const sess=fbaSess();
            try{const r=await apiKald({a:"notkatgem",token:sess.token,kategorier:liste});
                if(r&&r.ok){notKatPanel=false;notKatFilter=null;await notHent(true);renderNoter();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
        };
    }
    el.querySelectorAll(".not-k[data-h]").forEach(k=>{k.onclick=async()=>{
        const h2=k.getAttribute("data-h"),id=k.getAttribute("data-id"),sess=fbaSess();
        if(h2==="redig"){notRedig=id;renderNoter();return;}
        k.disabled=true;
        const n=((notData&&notData.mine)||[]).concat((notData&&notData.arkiv)||[]).find(x=>x.id===id);
        let kald=null;
        if(h2==="pin")kald={a:"notstatus",nid:id,pin:n&&n.pin?0:1};
        if(h2==="top")kald={a:"notstatus",nid:id,top:n&&n.top?0:1};
        if(h2==="arkiv")kald={a:"notstatus",nid:id,arkiv:1};
        if(h2==="gendan")kald={a:"notstatus",nid:id,arkiv:0};
        if(h2==="afvis"){if(!confirm("Fjern sedlen fra din tavle?"))/**/{k.disabled=false;return;}kald={a:"notafvis",nid:id};}
        if(!kald){k.disabled=false;return;}
        try{const r=await apiKald(Object.assign({token:sess.token},kald));
            if(r&&r.ok){await notHent(true);renderNoter();try{window.dispatchEvent(new CustomEvent("hfNoter",{detail:{fra:"tavle"}}));}catch(e){}}
            else{k.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){k.disabled=false;}
    };});
    el.querySelectorAll(".nr-slet").forEach(k=>{k.onclick=async()=>{
        if(!confirm("Slet sedlen permanent?"))return;
        k.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"notslet",token:sess.token,nid:k.getAttribute("data-id")});
            if(r&&r.ok){notRedig=null;await notHent(true);renderNoter();try{window.dispatchEvent(new CustomEvent("hfNoter",{detail:{fra:"tavle"}}));}catch(e){}}
            else{k.disabled=false;alert((r&&r.fejl)||"Kunne ikke slette");}}
        catch(e){k.disabled=false;}
    };});
    el.querySelectorAll(".not-redig").forEach(kort=>{
        let farve=null;
        kort.querySelectorAll(".nr-farve").forEach(f=>{f.onclick=()=>{
            farve=f.getAttribute("data-f");
            kort.style.background=NOT_HEX[farve];
            kort.querySelectorAll(".nr-farve").forEach(x=>x.style.border="2px solid "+(x===f?"#1e374c":"rgba(30,55,76,.15)"));
        };});
        const gem=kort.querySelector(".nr-gem");
        if(gem)gem.onclick=async function(){
            const txt=kort.querySelector(".nr-txt").value.trim();
            if(!txt){alert("Skriv noget på sedlen først.");return;}
            this.disabled=true;const sess=fbaSess();
            const id=kort.getAttribute("data-id");
            const n=id?((notData&&notData.mine)||[]).find(x=>x.id===id):null;
            const til=[];kort.querySelectorAll(".nr-til:checked").forEach(c=>til.push(c.value));
            const katV=kort.querySelector(".nr-kat")?kort.querySelector(".nr-kat").value:"";
            const fristV=kort.querySelector(".nr-frist")?kort.querySelector(".nr-frist").value:"";
            const fristKlV=kort.querySelector(".nr-fristkl")?kort.querySelector(".nr-fristkl").value:"";
            try{const r=await apiKald({a:"notgem",token:sess.token,nid:id||"",txt:txt,kat:katV,frist:fristV,fristKl:fristKlV,
                farve:farve||(n?n.farve:"gul"),til:til,pin:kort.querySelector(".nr-pin").checked?1:0});
                if(r&&r.ok){notRedig=null;await notHent(true);renderNoter();try{window.dispatchEvent(new CustomEvent("hfNoter",{detail:{fra:"tavle"}}));}catch(e){}}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
        };
        const tgWrap=kort.querySelector(".nr-tg");
        if(tgWrap){
            const fyldTg=function(){
                const brug=(hfTagGrp||[]).filter(t=>t.maaBruge&&(t.medlemmer||[]).length);
                tgWrap.innerHTML=brug.length?('<span style="font-size:.72rem;color:#2a3a48">Tilføj gruppe:</span>'+brug.map(t=>'<button type="button" class="nr-tg-b" data-g="'+fbaEsc(t.gid)+'" title="Markér alle i gruppen" style="border:1px solid '+fbaEsc(t.farve||"#3cb4e5")+';background:rgba(255,255,255,.6);border-radius:999px;padding:3px 9px;cursor:pointer;font-family:var(--font);font-size:.72rem;font-weight:700">🏷️ '+fbaEsc(t.navn)+'</button>').join("")):"";
                tgWrap.querySelectorAll(".nr-tg-b").forEach(b=>{b.onclick=()=>{
                    const t=(hfTagGrp||[]).filter(z=>z.gid===b.getAttribute("data-g"))[0];if(!t)return;
                    (t.medlemmer||[]).forEach(l=>{const cb=kort.querySelector('.nr-til[value="'+String(l).replace(/"/g,'\\"')+'"]');if(cb)cb.checked=true;});
                };});
            };
            fyldTg();hfTagGrpHent().then(fyldTg);
        }
        const fryd=kort.querySelector(".nr-frist-ryd");
        if(fryd)fryd.onclick=()=>{const a=kort.querySelector(".nr-frist"),b=kort.querySelector(".nr-fristkl");if(a)a.value="";if(b)b.value="";};
        const ft=kort.querySelector(".nr-fortryd");
        if(ft)ft.onclick=()=>{notRedig=null;renderNoter();};
    });
    el.querySelectorAll(".not-k").forEach(k=>{if(!k.style.cssText)k.style.cssText="border:0;background:rgba(255,255,255,.55);border-radius:7px;cursor:pointer;font-family:var(--font);font-size:.72rem;font-weight:800;color:#2a3a48;padding:3px 8px";});
}
function progLaesRedigering(){   /* saml fælles-redigeringens felter ind i progData.alle før gen-tegning/gem */
    if(!progData||!progData.alle)return;
    document.querySelectorAll("#progWrap .pr-kort").forEach(kort=>{
        const p=progData.alle[parseInt(kort.getAttribute("data-ix"),10)];if(!p)return;
        p.ikon=kort.querySelector(".pr-ikon").value.trim()||"🔗";
        p.navn=kort.querySelector(".pr-navn").value.trim();
        p.url=kort.querySelector(".pr-url").value.trim();
        p.logo=kort.querySelector(".pr-logo").value.trim();
        p.besk=kort.querySelector(".pr-besk").value.trim();
        p.guide=kort.querySelector(".pr-guide").value.trim();
        p.nyfane=kort.querySelector(".pr-nyfane").checked?1:0;
        var _hs=kort.querySelector(".pr-hs");if(_hs)p.hurtigstart=_hs.checked?1:0;
        p.grupper=[];kort.querySelectorAll(".pr-grp:checked").forEach(c=>p.grupper.push(c.getAttribute("data-g")));
        /* p.over vedligeholdes direkte af chip-/søge-handlerne */
    });
}
/* samme områdemenu på Intern- og Nyheds-siderne — punkter navigerer tilbage til centralen */
function admMenuIndsaet(id,aktiv){
    const el=document.getElementById(id);if(!el)return;
    if(!fbaSess()){el.innerHTML="";return;}
    let ret={fs:1,hjul:1,stat:1,ret:0};
    try{const c=JSON.parse(sessionStorage.getItem("adm_ret")||"null");if(c)ret=c;}catch(e){}
    const pkt=[["dash","🏠 Overblik"],["chat","💬 Live-chat"],["feedback","📬 Feedback"],["fs","🌍 Fællesskab"],
        ["ai","🤖 AI & viden"],["borgere","👥 Brugere"],["hjul","🗓 Årshjul"],["stat","📊 Tilfredshed"]];
    el.innerHTML=pkt.filter(p=>({fs:ret.fs,hjul:ret.hjul,stat:ret.stat}[p[0]]===undefined||{fs:ret.fs,hjul:ret.hjul,stat:ret.stat}[p[0]])).map(p=>
            '<button data-o="'+p[0]+'">'+p[1]+'</button>').join("")+
        '<button data-href="#/intern" class="'+(aktiv==="intern"?"aktiv":"")+'">🗨 Intern</button>'+
        '<button data-href="#/nyheds-admin" class="'+(aktiv==="nyheder"?"aktiv":"")+'">📣 Nyheder</button>'+
        (ret.ret?'<button data-o="ret">⚙️ Rettigheder</button>':'');
    el.querySelectorAll("button[data-o]").forEach(b=>{b.onclick=()=>{
        try{sessionStorage.setItem("omr_fokus",b.getAttribute("data-o"));}catch(e){}
        location.hash="#/feedback-admin";
    };});
    el.querySelectorAll("button[data-href]").forEach(b=>{b.onclick=()=>{location.hash=b.getAttribute("data-href");};});
}

/* ===================== ⏰ SLA: åbne beskeder der venter på os ===================== */
function slaHverdage(fraTs){
    const d=new Date(fraTs*1000);d.setHours(0,0,0,0);
    const nu=new Date();nu.setHours(0,0,0,0);
    let n=0;
    while(d<nu){d.setDate(d.getDate()+1);const wd=d.getDay();if(wd!==0&&wd!==6)n++;}
    return n;
}
function slaOver(b){   /* >0 = antal hverdage tråden har ventet på VORES svar */
    if(b.done)return 0;
    let sidste=b.ts||0,fraOs=false;
    (b.svar||[]).forEach(sv=>{if((sv.ts||0)>=sidste){sidste=sv.ts;fraOs=sv.fra!=="borger";}});
    if(fraOs)return 0;   /* sidste ord er vores → bolden ligger hos borgeren */
    const n=slaHverdage(sidste);
    return n>=2?n:0;
}

/* ===================== ☀️ Dagens overblik (digest-panelet) ===================== */
let digestTal=null;
async function renderDigest(){
    const el=document.getElementById("lcaDigest");if(!el||!lcaData)return;
    if(!digestTal){
        try{const j=await apiKald({a:"digesttal",token:fbaSess().token});if(j&&j.ok)digestTal=j;}catch(e){}
        if(!digestTal)digestTal={};
    }
    const venter=(lcaData.samtaler||[]).filter(s=>s.status==="venter").length,
        mine=(lcaData.samtaler||[]).filter(s=>s.nyt).length,
        nv=(lcaData.naevnt||[]).length;
    let aabneFb=null,overSLA=0;
    if(fbaData){
        const alle=(fbaData||[]).concat(fbaHub?fbaHub.feedback:[]);
        aabneFb=alle.filter(b=>!b.done).length;
        overSLA=alle.filter(b=>slaOver(b)).length;
    }
    if(window.hfTael&&typeof window.hfTael.feedback==="number")aabneFb=window.hfTael.feedback;   /* friskt tal fra 15-sek-pulsen */
    const frister=await notFristAktive();   /* ⏰ frist-noter hentes FØR rækken tegnes (ingen reload/dublet) */
    const chip=(ik,t,rød,gaa)=>'<span class="fba-side"'+(gaa?' data-gaa="'+gaa+'"':'')+' style="font-size:.83rem'+(rød?';background:rgba(231,0,76,.13);color:var(--roed)':'')+(gaa?';cursor:pointer':'')+'">'+ik+' '+t+'</span>';
    el.innerHTML='<div class="fba-kort" style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;border-left:5px solid var(--groen)">'+
        '<b style="margin-right:4px">☀️ Dagens overblik</b>'+
        chip("💬",venter+" venter i chat",venter>0,"chat")+
        (mine?chip("🆕",mine+" nye chatsvar",true,"chat"):"")+
        (nv?chip("📥",nv+" nævnt",true):"")+
        (aabneFb===null?"":chip("📬",aabneFb+" åbne beskeder",false,"side"))+
        (overSLA?chip("⏰",overSLA+" over 2 hverdage",true,"side"):"")+
        (lcaData.fsAfventer?chip("🌍",lcaData.fsAfventer+" til godkendelse",true,"fs"):"")+
        notFristSamlet(frister)+
        '<span style="margin-left:auto;color:var(--graa);font-size:.8rem">I går: '+
        (digestTal.chatsIGaar||0)+' chats · '+(digestTal.aiIGaar||0)+' AI-spørgsmål · '+(digestTal.loginsIGaar||0)+' logins</span></div>';
    /* klikbare chips: hop direkte til det rigtige OMRÅDE */
    el.querySelectorAll("[data-gaa]").forEach(c=>{
        c.onclick=()=>{
            const g=c.getAttribute("data-gaa");
            visOmraade(g==="fs"?"fs":g==="chat"?"chat":"feedback");
        };
    });
    el.querySelectorAll(".notfrist-chip").forEach(function(c){c.onclick=function(){try{sessionStorage.setItem("omr_fokus","noter");}catch(e){}visOmraade("noter");};});
    renderGenveje();   /* dashboard-kortene følger med tallene */
}

/* ===================== 🗓 Årshjul (admin) ===================== */
let hjulData=null;
async function renderHjulAdmin(){
    const boks=document.getElementById("hjulBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    try{const j=await apiKald({a:"aarshjuladmin",token:sess.token});if(j&&j.ok)hjulData=j;}catch(e){}
    if(!hjulData){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    const kanA=!!hjulData.retAlle,kanB=!!hjulData.retBorger;
    const gTekst=g=>g==="maaned"?"hver måned":g==="aar"?"hvert år":"én gang";
    const raekke=(p,kan)=>'<div class="fba-top" style="margin-top:6px;padding:8px 12px;background:#fff;border-radius:10px;border:1px solid rgba(30,55,76,.07)">'+
        '<b>'+fbaEsc(p.titel)+'</b>'+
        (p.type==="borger"?'<span class="fba-side">'+fbaEsc(ctNavn(p.u)||p.u)+'</span>':'')+
        '<span style="color:var(--graa);font-size:.82rem">'+(p.naeste?hjulDatoKort(p.naeste):"—")+' · '+gTekst(p.gentag)+' · vises '+p.visFoer+' dage før</span>'+
        (kan?'<button class="fba-done hj-ret" data-id="'+p.id+'" style="margin-left:auto">✏️</button><button class="fba-done hj-slet" data-id="'+p.id+'">🗑</button>':'')+'</div>';
    const fa=(hjulData.punkter||[]).filter(p=>p.type==="faelles").sort((a,b)=>a.naeste-b.naeste),
        bo=(hjulData.punkter||[]).filter(p=>p.type==="borger").sort((a,b)=>a.naeste-b.naeste);
    boks.innerHTML='<b>📢 Fælles — alle borgere'+(kanA?'':' <span style="color:var(--graa);font-weight:400;font-size:.8rem">(din rolle kan ikke redigere)</span>')+'</b>'+
        (fa.map(p=>raekke(p,kanA)).join("")||'<p style="color:var(--graa);font-size:.85rem;margin:4px 0">Ingen.</p>')+
        '<b style="display:block;margin-top:12px">👤 Pr. borger'+(kanB?'':' <span style="color:var(--graa);font-weight:400;font-size:.8rem">(din rolle kan ikke redigere)</span>')+'</b>'+
        (bo.map(p=>raekke(p,kanB)).join("")||'<p style="color:var(--graa);font-size:.85rem;margin:4px 0">Ingen.</p>')+
        ((kanA||kanB)?'<div style="display:grid;gap:8px;margin-top:12px;background:#fff;border-radius:12px;padding:12px;border:1px dashed rgba(30,55,76,.2)">'+
            '<b id="hjOverskrift">➕ Nyt punkt</b>'+
            '<div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">'+
            '<select id="hjType" style="font-family:var(--font);padding:7px 10px;border-radius:9px;border:1px solid rgba(30,55,76,.2)">'+
            (kanA?'<option value="faelles">📢 Fælles (alle borgere)</option>':'')+
            (kanB?'<option value="borger">👤 Pr. borger</option>':'')+'</select>'+
            '<input id="hjU" placeholder="Borgernummer (fx 200400)" hidden style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:7px 11px;font-family:var(--font);width:180px"></div>'+
            '<input id="hjTitel" maxlength="90" placeholder="Titel (det borgeren ser i striben)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font)">'+
            '<textarea id="hjTekst" rows="2" maxlength="400" placeholder="Uddybende tekst (valgfri)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font)"></textarea>'+
            '<div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">'+
            '<label style="font-size:.85rem">Dato <input id="hjDato" type="date" style="font-family:var(--font);padding:6px 9px;border-radius:8px;border:1px solid rgba(30,55,76,.2)"></label>'+
            '<label style="font-size:.85rem">Gentagelse <select id="hjGentag" style="font-family:var(--font);padding:6px 9px;border-radius:8px;border:1px solid rgba(30,55,76,.2)">'+
            '<option value="">Én gang</option><option value="maaned">Hver måned</option><option value="aar">Hvert år</option></select></label>'+
            '<label style="font-size:.85rem">Vis <input id="hjFoer" type="number" min="0" max="60" value="7" style="width:60px;font-family:var(--font);padding:6px 9px;border-radius:8px;border:1px solid rgba(30,55,76,.2)"> dage før</label></div>'+
            '<div style="display:flex;gap:8px"><button id="hjGem" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">💾 Gem</button>'+
            '<button id="hjNulstil" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.83rem" hidden>Annullér redigering</button></div></div>':'');
    if(!(kanA||kanB))return;
    let hjRedig="";
    const typeSel=document.getElementById("hjType"),uFelt=document.getElementById("hjU");
    function typeOpd(){uFelt.hidden=typeSel.value!=="borger";}
    typeSel.onchange=typeOpd;typeOpd();
    boks.querySelectorAll(".hj-ret").forEach(b=>{b.onclick=()=>{
        const p=(hjulData.punkter||[]).find(x=>x.id===b.getAttribute("data-id"));if(!p)return;
        hjRedig=p.id;
        typeSel.value=p.type;typeOpd();
        uFelt.value=p.u||"";
        document.getElementById("hjTitel").value=p.titel;
        document.getElementById("hjTekst").value=p.tekst||"";
        document.getElementById("hjDato").value=p.dato;
        document.getElementById("hjGentag").value=p.gentag||"";
        document.getElementById("hjFoer").value=p.visFoer;
        document.getElementById("hjOverskrift").textContent="✏️ Redigér: "+p.titel;
        document.getElementById("hjNulstil").hidden=false;
    };});
    boks.querySelectorAll(".hj-slet").forEach(b=>{b.onclick=async ()=>{
        if(!confirm("Slet punktet fra årshjulet?"))return;
        try{const r=await apiKald({a:"aarshjulslet",token:sess.token,id:b.getAttribute("data-id")});
            if(r&&r.ok)renderHjulAdmin();else alert((r&&r.fejl)||"Kunne ikke slette");}catch(e){}
    };});
    document.getElementById("hjNulstil").onclick=()=>renderHjulAdmin();
    document.getElementById("hjGem").onclick=async function(){
        this.disabled=true;
        try{const r=await apiKald({a:"aarshjulgem",token:sess.token,id:hjRedig,
            type:typeSel.value,u:uFelt.value.trim(),
            titel:document.getElementById("hjTitel").value.trim(),
            tekst:document.getElementById("hjTekst").value.trim(),
            dato:document.getElementById("hjDato").value,
            gentag:document.getElementById("hjGentag").value,
            visFoer:parseInt(document.getElementById("hjFoer").value,10)||7});
            if(r&&r.ok)renderHjulAdmin();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
}

/* ===================== 🏖 Lukkedage (kun ejeren) ===================== */
async function renderLukkedage(){
    const boks=document.getElementById("lukBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    let j=null;
    try{j=await apiKald({a:"lclukkedage",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    const idag=new Date().toISOString().slice(0,10);
    const helKommende=Object.keys(j.helligdage||{}).filter(d=>d>=idag).sort().slice(0,6);
    boks.innerHTML='<p style="color:var(--graa);font-size:.83rem">På lukkedage ser borgerne ikke chat-knappen, uanset åbningstider. Danske helligdage er lukket automatisk.</p>'+
        '<b style="font-size:.9rem">Kommende helligdage (automatisk)</b><div style="color:var(--graa);font-size:.85rem;margin:4px 0 10px">'+
        (helKommende.map(d=>d.split("-").reverse().join(".")+" · "+fbaEsc(j.helligdage[d])).join("<br>")||"—")+'</div>'+
        '<b style="font-size:.9rem">Egne lukkedage</b>'+
        (Object.keys(j.dage||{}).sort().map(d=>'<div class="fba-top" style="margin-top:5px;padding:7px 11px;background:#fff;border-radius:9px;border:1px solid rgba(30,55,76,.07)">'+
            '<b>'+d.split("-").reverse().join(".")+'</b><span style="color:var(--graa)">'+fbaEsc(j.dage[d]||"")+'</span>'+
            '<button class="fba-done luk-fjern" data-d="'+d+'" style="margin-left:auto">🗑</button></div>').join("")||'<p style="color:var(--graa);font-size:.85rem;margin:4px 0">Ingen.</p>')+
        '<div style="display:flex;gap:8px;margin-top:10px;flex-wrap:wrap;align-items:center">'+
        '<input id="lukDato" type="date" style="font-family:var(--font);padding:7px 10px;border-radius:9px;border:1px solid rgba(30,55,76,.2)">'+
        '<input id="lukNote" maxlength="80" placeholder="Note (fx «juleferie»)" style="flex:1;min-width:150px;border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:7px 11px;font-family:var(--font)">'+
        '<button id="lukTilfoej" class="fba-send" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">➕ Tilføj</button></div>';
    boks.querySelectorAll(".luk-fjern").forEach(b=>{b.onclick=async ()=>{
        try{const r=await apiKald({a:"lclukkedage",token:sess.token,fjern:b.getAttribute("data-d")});
            if(r&&r.ok)renderLukkedage();else alert((r&&r.fejl)||"Kunne ikke fjerne");}catch(e){}
    };});
    document.getElementById("lukTilfoej").onclick=async function(){
        const d=document.getElementById("lukDato").value;
        if(!d){alert("Vælg en dato.");return;}
        this.disabled=true;
        try{const r=await apiKald({a:"lclukkedage",token:sess.token,tilfoej:{dato:d,note:document.getElementById("lukNote").value.trim()}});
            if(r&&r.ok)renderLukkedage();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
}

/* ===================== ➕ Opret brugere (perm admopret / borgeropret) ===================== */
function renderOpret(ret){
    const boks=document.getElementById("opretBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    boks.innerHTML=
        (ret.borgeropret?'<div style="background:#fff;border-radius:12px;padding:14px;border:1px dashed rgba(30,55,76,.2);margin-bottom:12px">'+
            '<b>👤 Ny borger-bruger</b>'+
            '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 10px">Borgeren logger ind med startkoden 1234 og vælger sit eget kodeord første gang.</p>'+
            '<div style="display:flex;gap:10px;flex-wrap:wrap">'+
            '<input id="obU" maxlength="6" placeholder="Kundenummer (6 cifre, fx 200455)" title="Borger-brugernavne er kundenummeret: 6 cifre der starter med 20" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);width:220px">'+
            '<input id="obNavn" maxlength="120" placeholder="Fulde navn" style="flex:1;min-width:180px;border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font)">'+
            '<select id="obKons" style="font-family:var(--font);padding:8px 10px;border-radius:9px;border:1px solid rgba(30,55,76,.2);max-width:230px"><option value="">🤝 Konsulent (vælg evt. nu)</option></select>'+
            '<input id="obMid" maxlength="8" placeholder="mitHF-id / kunde-id (fx 5783)" title="Borgerens id i mitHF (Temponizer brugere_id) — bruges til det direkte link og senere til login. Skal være unikt." style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);width:210px">'+
            '<button id="obOpret" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">➕ Opret borger</button></div></div>':'')+
        (ret.admopret?'<div style="background:#fff;border-radius:12px;padding:14px;border:1px dashed rgba(30,55,76,.2)">'+
            '<b>🔧 Ny admin-bruger</b>'+
            '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 10px">Interne, der allerede har et login til universet, får i stedet admin-adgang under ⚙️ Rettigheder.</p>'+
            '<div style="display:flex;gap:10px;flex-wrap:wrap">'+
            '<input id="oaLogin" maxlength="60" placeholder="Login (små bogstaver)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);width:170px">'+
            '<input id="oaVis" maxlength="60" placeholder="Visningsnavn (fx Cecilie)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);width:190px">'+
            '<select id="oaGruppe" style="font-family:var(--font);padding:8px 10px;border-radius:9px;border:1px solid rgba(30,55,76,.2)">'+
            Object.keys(GRP_NAVNE).map(g=>'<option value="'+g+'"'+(g==="konsulent"?" selected":"")+'>'+GRP_NAVNE[g]+'</option>').join("")+'</select>'+
            '<input id="oaKode" type="text" maxlength="60" placeholder="Start-kodeord (min. 6 tegn)" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);width:200px">'+
            '<button id="oaOpret" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">➕ Opret admin</button></div></div>':'');
    const ob=document.getElementById("obOpret");
    if(ob)ob.onclick=async function(){
        const u=document.getElementById("obU").value.trim(),navn=document.getElementById("obNavn").value.trim();
        if(!/^20\d{4}$/.test(u)){alert("Borger-brugernavne er kundenummeret: 6 cifre der starter med 20, fx 200412.");return;}
        if(navn.length<2){alert("Skriv borgerens navn.");return;}
        const midV=(document.getElementById("obMid")||{value:""}).value.trim();
        if(midV&&!/^\d{1,8}$/.test(midV)){alert("mitHF-id (kunde-id) er et tal, fx 5783.");return;}
        this.disabled=true;
        const konsV=(document.getElementById("obKons")||{value:""}).value;
        try{const r=await apiKald({a:"borgeropretbruger",token:sess.token,u:u,navn:navn,kons:konsV,mid:midV});
            if(r&&r.ok){
                if(r.kode)visMidlKode(navn,u,r.kode);else alert("✅ "+navn+" er oprettet.");
                document.getElementById("obU").value="";document.getElementById("obNavn").value="";
                if(document.getElementById("obMid"))document.getElementById("obMid").value="";
                if(document.getElementById("obKons"))document.getElementById("obKons").value="";
                fbaAdgang=null;fbaKons=null;}
            else alert((r&&r.fejl)||"Kunne ikke oprette");}
        catch(e){alert("Kunne ikke oprette — tjek forbindelsen.");}
        this.disabled=false;
    };
    /* konsulent-vælgeren fyldes fra kataloget (hentes ved behov) */
    (async()=>{
        const sel=document.getElementById("obKons");if(!sel)return;
        try{
            if(!fbaKons){const j=await apiKald({a:"konsdata",token:sess.token});if(j&&j.ok)fbaKons={kat:j.katalog||{},bor:j.borgere||{}};}
            if(fbaKons)Object.keys(fbaKons.kat).sort((a,b)=>a.localeCompare(b,"da"))
                .forEach(n=>{const o=document.createElement("option");o.value=n;o.textContent=n;sel.appendChild(o);});
        }catch(e){}
    })();
    const oa=document.getElementById("oaOpret");
    if(oa)oa.onclick=async function(){
        const login=document.getElementById("oaLogin").value.trim(),kode=document.getElementById("oaKode").value.trim();
        if(!login||kode.length<6){alert("Udfyld login og et kodeord på mindst 6 tegn.");return;}
        this.disabled=true;
        try{const r=await apiKald({a:"admopretbruger",token:sess.token,login:login,vis:document.getElementById("oaVis").value.trim(),
            gruppe:document.getElementById("oaGruppe").value,kodeord:kode});
            if(r&&r.ok){alert("✅ Admin-brugeren "+login+" er oprettet.");retData=null;
                document.getElementById("oaLogin").value="";document.getElementById("oaVis").value="";document.getElementById("oaKode").value="";}
            else alert((r&&r.fejl)||"Kunne ikke oprette");}
        catch(e){alert("Kunne ikke oprette — tjek forbindelsen.");}
        this.disabled=false;
    };
}

/* ===================== 📚 Vidensbase-redigering (kun ejeren) ===================== */
async function renderViden(){
    const boks=document.getElementById("videnBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    let j=null;
    try{j=await apiKald({a:"videnhent",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    boks.innerHTML='<p style="color:var(--graa);font-size:.83rem">Ret teksten og gem — chatten bruger den nye version med det samme. Der tages automatisk backup af de 10 seneste versioner på serveren.</p>'+
        ((j.udkast||[]).length?'<b style="font-size:.9rem">💡 Udkast fra kollegerne ('+j.udkast.length+')</b>'+
            j.udkast.map(u=>'<div style="background:#fff8e1;border:1px solid rgba(200,160,40,.35);border-radius:10px;padding:9px 12px;margin:6px 0;font-size:.85rem">'+
                '<b>'+fbaEsc(visA(u.af||""))+'</b> <span style="color:var(--graa);font-size:.75rem">'+fbaDato(u.ts||0)+'</span>'+
                '<div style="white-space:pre-wrap;margin-top:4px">'+fbaEsc(u.txt||"")+'</div>'+
                '<div style="margin-top:6px"><button class="fba-done vu-ind" data-id="'+u.id+'" style="border:0;border-radius:8px;padding:6px 11px;cursor:pointer;font-size:.78rem">⤵ Indsæt nederst i teksten</button> '+
                '<button class="fba-done vu-klar" data-id="'+u.id+'" style="border:0;border-radius:8px;padding:6px 11px;cursor:pointer;font-size:.78rem">✓ Fjern fra listen</button></div></div>').join(""):'')+
        '<textarea id="videnTekst" spellcheck="false" style="width:100%;box-sizing:border-box;min-height:420px;margin-top:8px;border:1px solid rgba(30,55,76,.2);border-radius:12px;padding:12px;font-family:ui-monospace,Menlo,Consolas,monospace;font-size:.82rem;line-height:1.5"></textarea>'+
        '<div style="display:flex;gap:10px;margin-top:8px;align-items:center">'+
        '<button id="videnGem" class="fba-send" style="border:0;border-radius:10px;padding:10px 18px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.88rem">💾 Gem vidensbasen</button>'+
        '<span id="videnStatus" style="color:var(--graa);font-size:.82rem"></span></div>';
    document.getElementById("videnTekst").value=j.indhold||"";
    boks.querySelectorAll(".vu-ind").forEach(b=>{b.onclick=()=>{
        const u=(j.udkast||[]).find(x=>x.id===b.getAttribute("data-id"));if(!u)return;
        const ta=document.getElementById("videnTekst");
        ta.value=ta.value.replace(/\s*$/,"")+"\n\n"+u.txt+"\n";
        ta.scrollTop=ta.scrollHeight;
        b.textContent="✔ indsat";b.disabled=true;
    };});
    boks.querySelectorAll(".vu-klar").forEach(b=>{b.onclick=async ()=>{
        try{const r=await apiKald({a:"videnudkastklar",token:sess.token,id:b.getAttribute("data-id")});
            if(r&&r.ok)b.parentNode.parentNode.remove();}catch(e){}
    };});
    document.getElementById("videnGem").onclick=async function(){
        if(!confirm("Gem vidensbasen?\nChatten bruger den nye version med det samme (backup tages automatisk)."))return;
        this.disabled=true;
        const st=document.getElementById("videnStatus");
        try{const r=await apiKald({a:"videngem",token:sess.token,indhold:document.getElementById("videnTekst").value});
            if(r&&r.ok){st.textContent="✔ Gemt "+new Date().toLocaleTimeString("da-DK",{hour:"2-digit",minute:"2-digit"});}
            else{st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        this.disabled=false;
    };
}

/* ===================== 🤝 HF-assistent — fane + medarbejder-vidensbase ===================== */
function renderAssistentFane(){
    const b=document.getElementById("asistAabn");
    if(b&&!b._wired){b._wired=true;b.onclick=()=>{if(window.HFAsist)window.HFAsist.aabn();};}
}
async function renderMViden(){
    const boks=document.getElementById("mvidenBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    let j=null;
    try{j=await apiKald({a:"mvidenhent",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    boks.innerHTML='<p style="color:var(--graa);font-size:.83rem">Ret teksten og gem — HF-assistenten bruger den nye version med det samme. Der tages automatisk backup af de 10 seneste versioner på serveren.</p>'+
        '<textarea id="mvidenTekst" spellcheck="false" style="width:100%;box-sizing:border-box;min-height:420px;margin-top:8px;border:1px solid rgba(30,55,76,.2);border-radius:12px;padding:12px;font-family:ui-monospace,Menlo,Consolas,monospace;font-size:.82rem;line-height:1.5"></textarea>'+
        '<div style="display:flex;gap:10px;margin-top:8px;align-items:center">'+
        '<button id="mvidenGem" style="border:0;border-radius:10px;padding:10px 18px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.88rem;color:#0d2a12;background:var(--groen)">💾 Gem assistentens viden</button>'+
        '<span id="mvidenStatus" style="color:var(--graa);font-size:.82rem"></span></div>';
    document.getElementById("mvidenTekst").value=j.indhold||"";
    document.getElementById("mvidenGem").onclick=async function(){
        if(!confirm("Gem assistentens vidensbase?\nHF-assistenten bruger den nye version med det samme (backup tages automatisk)."))return;
        this.disabled=true;
        const st=document.getElementById("mvidenStatus");
        try{const r=await apiKald({a:"mvidengem",token:sess.token,indhold:document.getElementById("mvidenTekst").value});
            if(r&&r.ok){st.textContent="✔ Gemt "+new Date().toLocaleTimeString("da-DK",{hour:"2-digit",minute:"2-digit"});}
            else{st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        this.disabled=false;
    };
}
/* grøn medarbejder-chat (flydende, kun i admin) */
window.HFAsist=(function(){
    const fab=document.getElementById("aFab"),win=document.getElementById("aWin"),luk=document.getElementById("aLuk"),
        log=document.getElementById("aLog"),inp=document.getElementById("aInput"),send=document.getElementById("aSend");
    if(!fab||!win||!log||!inp)return {aabn:function(){}};
    const hist=[];let velkomst=false,skrivEl=null;
    function tid(){const d=new Date(),p=n=>(n<10?"0":"")+n;return p(d.getHours())+":"+p(d.getMinutes());}
    function boble(tekst,type){
        const d=document.createElement("div");d.className="c-b "+type;d.textContent=tekst;log.appendChild(d);
        if(type!=="sys"){const t=document.createElement("span");t.className="c-tid";t.textContent=tid();log.appendChild(t);}
        log.scrollTop=log.scrollHeight;return d;
    }
    function visSkriv(){if(skrivEl)return;skrivEl=document.createElement("div");skrivEl.className="c-skriv";skrivEl.innerHTML="<i></i><i></i><i></i>";log.appendChild(skrivEl);log.scrollTop=log.scrollHeight;}
    function fjernSkriv(){if(skrivEl){skrivEl.remove();skrivEl=null;}}
    async function svar(q){
        visSkriv();
        let adm=null;try{adm=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
        if(!(adm&&adm.token)){fjernSkriv();boble("Du skal være logget ind som medarbejder for at bruge assistenten.","sys");return;}
        try{
            const j=await apiKald({a:"assistent",token:adm.token,sp:q,hist:hist.slice(-6)});
            fjernSkriv();
            if(j&&j.ok&&j.svar){boble(j.svar,"bot");hist.push({r:"hf",t:j.svar});return;}
            boble((j&&j.fejl)||"Jeg kunne ikke svare lige nu — prøv igen om lidt.","sys");
        }catch(e){fjernSkriv();boble("Kunne ikke få fat i assistenten — tjek forbindelsen.","sys");}
    }
    function sendMsg(){const v=inp.value.trim();if(!v)return;boble(v,"mig");hist.push({r:"medarb",t:v});inp.value="";inp.style.height="auto";svar(v);}
    function aabn(){
        const bw=document.getElementById("chatWin");if(bw)bw.classList.remove("vis");   /* luk borger-chatten hvis åben */
        win.classList.add("vis");
        if(!velkomst){velkomst=true;boble("Hej 👋 Jeg er HF-assistenten — spørg løs! Jeg kender systemerne (mitHF/Temponizer), reglerne, overenskomsten og de interne arbejdsgange på fingerspidserne, og en del mere. Hvad har du brug for?","bot");}
        setTimeout(()=>{try{inp.focus();}catch(e){}},80);
    }
    function luks(){win.classList.remove("vis");}
    fab.onclick=()=>{win.classList.contains("vis")?luks():aabn();};
    luk.onclick=luks;
    send.onclick=sendMsg;
    inp.addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();sendMsg();}});
    inp.addEventListener("input",()=>hfGrow(inp,120));
    /* vis knappen med det samme, hvis der allerede er en admin-session (perm er som standard slået til;
     Admin-centralen skjuler den igen, hvis rettigheden er taget fra brugeren) */
    try{const _a=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");if(_a&&_a.token)fab.style.display="grid";}catch(e){}
    return {aabn:aabn};
})();

/* ===================== 🔔 Notifikationer (Web Push) — kort på Overblik ===================== */
function pushB64(base64){
    const pad='='.repeat((4-base64.length%4)%4);
    const b=(base64+pad).replace(/-/g,'+').replace(/_/g,'/');
    const raw=atob(b); const arr=new Uint8Array(raw.length);
    for(let i=0;i<raw.length;i++)arr[i]=raw.charCodeAt(i);
    return arr;
}
function pushStandalone(){ return (window.matchMedia&&window.matchMedia('(display-mode: standalone)').matches)||window.navigator.standalone===true; }
async function renderPush(){
    const el=document.getElementById("admPush"); if(!el||admOmr!=="dash")return;
    const sess=fbaSess(); if(!sess){el.innerHTML="";return;}
    const kort=(inner)=>'<div class="fba-kort" style="border-left:5px solid var(--blaa);display:flex;align-items:center;gap:12px;flex-wrap:wrap">'+inner+'</div>';
    const B='border:0;border-radius:10px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.85rem';
    const iOS=/iphone|ipad|ipod/i.test(navigator.userAgent);
    if(!('serviceWorker' in navigator)||!('PushManager' in window)||!('Notification' in window)){
        if(iOS&&!pushStandalone()){ el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:var(--graa);font-size:.85rem">På iPhone: åbn <b>Del-ikonet</b> i Safari → «Føj til hjemmeskærm», og åbn så universet fra det nye ikon. Så kan du slå notifikationer til her (kræver iOS 16.4+).</span>'); }
        else if(iOS){ el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:var(--graa);font-size:.85rem">Notifikationer på iPhone kræver <b>iOS 16.4 eller nyere</b> — og at universet er åbnet fra app-ikonet på hjemmeskærmen.</span>'); }
        else { el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:var(--graa);font-size:.85rem">Denne browser understøtter ikke notifikationer. Prøv Chrome, Edge eller Safari — og at siden åbnes over https.</span>'); }
        return;
    }
    let sub=null;
    try{ const reg=await navigator.serviceWorker.ready; sub=await reg.pushManager.getSubscription(); }catch(e){}
    if(Notification.permission==='denied'){
        el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:var(--graa);font-size:.85rem">Er blokeret i browserens indstillinger for dette site. Tillad dem der for at kunne bruge dem.</span>'); return;
    }
    if(sub){
        el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:#41630a;font-weight:700;font-size:.88rem">✅ Slået til på denne enhed</span>'+
            '<span style="flex:1"></span>'+
            '<button id="pushTest" style="'+B+';background:var(--groen);color:#0d2a12">Send test</button>'+
            '<button id="pushFra" style="'+B+';background:#eef3f7;color:var(--moerk)">Slå fra</button>');
        document.getElementById("pushTest").onclick=async function(){ this.disabled=true; try{const r=await apiKald({a:"pushtest",token:sess.token}); alert(r&&r.ok?"Test sendt 🎉 — kig efter notifikationen (også hvis fanen er i baggrunden).":((r&&r.fejl)||"Kunne ikke sende testen."));}catch(e){alert("Kunne ikke sende testen.");} this.disabled=false; };
        document.getElementById("pushFra").onclick=async function(){ try{const reg=await navigator.serviceWorker.ready; const s=await reg.pushManager.getSubscription(); if(s){ await apiKald({a:"pushunsub",token:sess.token,endpoint:s.endpoint}); await s.unsubscribe(); } }catch(e){} renderPush(); };
    }else{
        el.innerHTML=kort('<b>🔔 Notifikationer</b><span style="color:var(--graa);font-size:.85rem">Få besked — også når browseren er lukket — når du bliver tagget, får en chat overdraget, eller en borger skriver.</span>'+
            '<span style="flex:1"></span><button id="pushTil" style="'+B+';background:var(--moerk);color:#fff">Slå til</button>');
        document.getElementById("pushTil").onclick=pushTilmeld;
    }
}
async function pushTilmeld(){
    const sess=fbaSess(); if(!sess)return;
    try{
        const perm=await Notification.requestPermission();
        if(perm!=='granted'){ alert("Du sagde nej til notifikationer. Du kan altid slå dem til igen i browserens indstillinger."); return; }
        const reg=await navigator.serviceWorker.ready;
        const j=await apiKald({a:"pushpub"});
        if(!(j&&j.ok&&j.public)){ alert("Serveren er ikke sat op til notifikationer endnu."); return; }
        const s=await reg.pushManager.subscribe({userVisibleOnly:true,applicationServerKey:pushB64(j.public)});
        const r=await apiKald({a:"pushsub",token:sess.token,sub:s.toJSON()});
        if(!(r&&r.ok)){ alert((r&&r.fejl)||"Kunne ikke gemme tilmeldingen."); }
        renderPush();
    }catch(e){ alert("Kunne ikke slå notifikationer til: "+(e&&e.message?e.message:e)); }
}

/* ===================== ✏️ Tekster — redigér borger-tekster (perm 'tekster') ===================== */
const TEKST_BLOKKE=[
    {omr:"🏠 Forsiden",blokke:[
            {id:"hero-tekst",navn:"Velkomst-tekst under overskriften",std:"Alt det vigtigste for din BPA-ordning — samlet ét sted …"}]},
    {omr:"❓ FAQ",blokke:[
            {id:"faq-titel",navn:"Overskrift",std:"❓ Ofte stillede spørgsmål"},
            {id:"faq-intro",navn:"Intro-tekst",std:"Svar på de spørgsmål, vi oftest får …"}]},
    {omr:"☎️ Kontakt",blokke:[
            {id:"kontakt-titel",navn:"Overskrift",std:"☎️ Kontakt & akut hjælp"},
            {id:"kontakt-intro",navn:"Intro-tekst",std:"Din faste konsulent er altid din primære indgang …"}]}
];
async function renderTekster(){
    const boks=document.getElementById("tekstBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    let j=null;try{j=await apiKald({a:"teksteradmin",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente — tjek adgang")+'</p>';return;}
    const ov=j.blokke||{};
    let faqArb=(Array.isArray(j.faq)&&j.faq.length)?j.faq.map(f=>({sp:f.sp||"",svar:f.svar||""})):FAQ_STD.map(f=>({sp:f.sp,svar:f.svar}));
    const FORSIDE_KASSER=[{id:"laering",ik:"🎓",n:"Læringshub",b:"Lær det nye mitHF"},{id:"onboarding",ik:"🧭",n:"Kom godt i gang",b:""},{id:"mithf",ik:"🗓️",n:"mitHF-BPA",b:""},{id:"beskeder",ik:"💬",n:"Beskeder & support",b:"Nyt!"},{id:"noter",ik:"📌",n:"Mine noter",b:""},{id:"kalender",ik:"📅",n:"Min kalender",b:"Nyt!"},{id:"faellesskab",ik:"🌍",n:"Fællesskabet",b:"Nyt!"},{id:"aarshjul",ik:"🗓",n:"Dit årshjul",b:""},{id:"formularer",ik:"📝",n:"Formularer & selvbetjening",b:""},{id:"kontakt",ik:"☎️",n:"Kontakt & akut hjælp",b:""},{id:"faq",ik:"❓",n:"Spørgsmål & svar",b:""},{id:"nyheder",ik:"📣",n:"Nyheder & arrangementer",b:"Nyt"},{id:"hjemmeside",ik:"🌐",n:"Vores hjemmeside",b:"Ny"}];
    const FK_NAVN={};FORSIDE_KASSER.forEach(k=>FK_NAVN[k.id]=k);
    let fkCfg={orden:[],skjul:[],navn:{},badge:{},tekst:{}};try{const fj=await apiKald({a:"forsidehent"});if(fj&&fj.ok)fkCfg={orden:fj.orden||[],skjul:fj.skjul||[],navn:fj.navn||{},badge:fj.badge||{},tekst:fj.tekst||{}};}catch(e){}
    const fkItem=id=>{const d=FK_NAVN[id]||{n:id,b:""};
        const navn=(fkCfg.navn&&fkCfg.navn[id]!==undefined)?fkCfg.navn[id]:d.n;
        const bcfg=(fkCfg.badge&&fkCfg.badge[id]!==undefined)?fkCfg.badge[id]:d.b;
        const tekst=(fkCfg.tekst&&fkCfg.tekst[id]!==undefined)?fkCfg.tekst[id]:"";
        return {id:id,skjul:fkCfg.skjul.indexOf(id)>=0,navn:navn,tekst:tekst,badgeOn:bcfg!=="",badgeTekst:bcfg!==""?bcfg:d.b};};
    let fkArb=[];fkCfg.orden.forEach(id=>{if(FK_NAVN[id]&&!fkArb.some(x=>x.id===id))fkArb.push(fkItem(id));});
    FORSIDE_KASSER.forEach(k=>{if(!fkArb.some(x=>x.id===k.id))fkArb.push(fkItem(k.id));});
    let h='<h2 style="margin:0 0 6px">✏️ Tekster på borger-universet</h2>'+
        '<p style="color:var(--graa);font-size:.85rem">Ret teksterne borgerne ser. Ændringer slår igennem <b>med det samme</b>. Lad et felt stå tomt for at bruge standard-teksten. Der tages automatisk backup af de 10 seneste versioner.</p>';
    TEKST_BLOKKE.forEach(g=>{
        h+='<div class="fba-kort" style="border-left:5px solid var(--blaa)"><b>'+g.omr+'</b>';
        g.blokke.forEach(b=>{
            const val=(ov[b.id]!==undefined)?ov[b.id]:"";
            h+='<div style="margin-top:9px"><label style="font-size:.82rem;color:var(--graa);font-weight:700;display:block;margin-bottom:3px">'+fbaEsc(b.navn)+'</label>'+
                '<textarea class="tk-blok" data-id="'+fbaEsc(b.id)+'" rows="2" placeholder="Tom = standardteksten bruges" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:10px;padding:8px 10px;font-family:var(--font);font-size:.88rem">'+fbaEsc(val)+'</textarea></div>';
        });
        h+='</div>';
    });
    h+='<div class="fba-kort" style="border-left:5px solid var(--groen)"><b>❓ FAQ — spørgsmål & svar</b>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 8px">Redigér, tilføj, slet og flyt op/ned. Svar må gerne indeholde links (fx &lt;a href=…&gt;).</p>'+
        '<div id="faqEdit"></div>'+
        '<div style="margin-top:6px"><button id="faqTilf" style="'+PROG_KNAP+'">➕ Nyt spørgsmål</button> '+
        '<button id="faqNulstil" style="'+PROG_KNAP+';color:var(--roed)">↩ Nulstil FAQ til standard</button></div></div>';
    h+='<div class="fba-kort" style="border-left:5px solid #8a6400"><b>🧩 Forsidens kasser</b>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 8px">Bestem rækkefølgen og vis/skjul de kasser, borgerne ser på forsiden. Skjulte kasser forsvinder fra borger-forsiden (siderne findes stadig).</p>'+
        '<div id="fkEdit"></div>'+
        '<div style="margin-top:8px"><button id="fkGem" style="border:0;border-radius:11px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.86rem;background:var(--blaa);color:#fff">💾 Gem forsidens rækkefølge</button> <span id="fkStatus" style="color:var(--graa);font-size:.82rem"></span></div></div>';
    h+='<div style="display:flex;gap:10px;margin-top:4px;align-items:center"><button id="tkGem" style="border:0;border-radius:11px;padding:11px 20px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.9rem;background:var(--groen);color:#0d2a12">💾 Gem alle tekster</button><span id="tkStatus" style="color:var(--graa);font-size:.83rem"></span></div>';
    boks.innerHTML=h;
    function fkTegn(){
        const el=document.getElementById("fkEdit");if(!el)return;
        el.innerHTML=fkArb.map((k,i)=>{const d=FK_NAVN[k.id]||{ik:"▫️"};
            return '<div class="fba-kort" style="box-shadow:none;border:1px solid rgba(30,55,76,.14);margin:6px 0;padding:9px 11px'+(k.skjul?';opacity:.6':'')+'">'+
                '<div style="display:flex;gap:8px;align-items:center">'+
                '<span style="font-size:1.1rem">'+d.ik+'</span>'+
                '<input class="fk-navn" data-i="'+i+'" value="'+fbaEsc(k.navn)+'" placeholder="Kassens titel" style="flex:1;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 9px;font-family:var(--font);font-size:.88rem;font-weight:700">'+
                '<button class="fk-op" data-i="'+i+'"'+(i===0?" disabled":"")+' style="'+PROG_KNAP+';padding:2px 9px">↑</button>'+
                '<button class="fk-ned" data-i="'+i+'"'+(i===fkArb.length-1?" disabled":"")+' style="'+PROG_KNAP+';padding:2px 9px">↓</button>'+
                '<button class="fk-vis" data-i="'+i+'" style="'+PROG_KNAP+';padding:2px 10px">'+(k.skjul?"👁 Vis":"🙈 Skjul")+'</button>'+
                '</div>'+
                '<div style="display:flex;gap:8px;align-items:center;margin-top:6px;padding-left:28px">'+
                '<label style="font-size:.8rem;color:var(--graa);display:flex;gap:5px;align-items:center;white-space:nowrap;cursor:pointer"><input type="checkbox" class="fk-badgeon" data-i="'+i+'"'+(k.badgeOn?" checked":"")+'> 🏷️ Grøn mærkat</label>'+
                '<input class="fk-badge" data-i="'+i+'" value="'+fbaEsc(k.badgeTekst)+'"'+(k.badgeOn?"":" disabled")+' placeholder="fx Nyt!" maxlength="60" style="flex:1;max-width:230px;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:5px 9px;font-family:var(--font);font-size:.84rem'+(k.badgeOn?"":";opacity:.45")+'">'+
                '</div>'+
                '<div style="margin-top:6px;padding-left:28px"><label style="font-size:.78rem;color:var(--graa);display:block;margin-bottom:3px">Ekstra tekst i kassen</label><textarea class="fk-tekst" data-i="'+i+'" rows="2" placeholder="Tom = standardteksten bruges" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 9px;font-family:var(--font);font-size:.84rem">'+fbaEsc(k.tekst||"")+'</textarea></div>'+
                '</div>';
        }).join("");
        el.querySelectorAll(".fk-navn").forEach(x=>x.oninput=()=>{fkArb[+x.getAttribute("data-i")].navn=x.value;});
        el.querySelectorAll(".fk-tekst").forEach(x=>x.oninput=()=>{fkArb[+x.getAttribute("data-i")].tekst=x.value;});
        el.querySelectorAll(".fk-badge").forEach(x=>x.oninput=()=>{fkArb[+x.getAttribute("data-i")].badgeTekst=x.value;});
        el.querySelectorAll(".fk-badgeon").forEach(x=>x.onchange=()=>{fkArb[+x.getAttribute("data-i")].badgeOn=x.checked;fkTegn();});
        el.querySelectorAll(".fk-op").forEach(b=>b.onclick=()=>{const i=+b.getAttribute("data-i");if(i>0){const t=fkArb[i-1];fkArb[i-1]=fkArb[i];fkArb[i]=t;fkTegn();}});
        el.querySelectorAll(".fk-ned").forEach(b=>b.onclick=()=>{const i=+b.getAttribute("data-i");if(i<fkArb.length-1){const t=fkArb[i+1];fkArb[i+1]=fkArb[i];fkArb[i]=t;fkTegn();}});
        el.querySelectorAll(".fk-vis").forEach(b=>b.onclick=()=>{const i=+b.getAttribute("data-i");fkArb[i].skjul=!fkArb[i].skjul;fkTegn();});
    }
    fkTegn();
    document.getElementById("fkGem").onclick=async function(){
        this.disabled=true;const st=document.getElementById("fkStatus");
        const navnMap={},badgeMap={},tekstMap={};
        fkArb.forEach(k=>{navnMap[k.id]=(k.navn||"").trim();badgeMap[k.id]=(k.badgeOn&&(k.badgeTekst||"").trim())?(k.badgeTekst||"").trim():"";tekstMap[k.id]=(k.tekst||"").trim();});
        try{const r=await apiKald({a:"forsidegem",token:sess.token,orden:fkArb.map(k=>k.id),skjul:fkArb.filter(k=>k.skjul).map(k=>k.id),navn:navnMap,badge:badgeMap,tekst:tekstMap});
            if(r&&r.ok){st.textContent="✔ Gemt — forsiden er opdateret";}else{st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        this.disabled=false;
    };
    function faqEditTegn(){
        const el=document.getElementById("faqEdit");if(!el)return;
        el.innerHTML=faqArb.map((f,i)=>'<div class="fba-kort" style="box-shadow:none;border:1px solid rgba(30,55,76,.14);margin:6px 0">'+
            '<div style="display:flex;gap:6px;align-items:center;margin-bottom:4px"><span style="font-weight:800;color:var(--graa);font-size:.8rem">#'+(i+1)+'</span><span style="flex:1"></span>'+
            '<button class="fq-op" data-i="'+i+'"'+(i===0?" disabled":"")+' style="'+PROG_KNAP+';padding:2px 8px">↑</button>'+
            '<button class="fq-ned" data-i="'+i+'"'+(i===faqArb.length-1?" disabled":"")+' style="'+PROG_KNAP+';padding:2px 8px">↓</button>'+
            '<button class="fq-slet" data-i="'+i+'" style="'+PROG_KNAP+';color:var(--roed);padding:2px 8px">🗑</button></div>'+
            '<input class="fq-sp" data-i="'+i+'" value="'+fbaEsc(f.sp)+'" placeholder="Spørgsmål" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:7px 9px;font-family:var(--font);font-size:.88rem;font-weight:700;margin-bottom:4px">'+
            '<textarea class="fq-svar" data-i="'+i+'" rows="3" placeholder="Svar (links tilladt)" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:7px 9px;font-family:var(--font);font-size:.86rem">'+fbaEsc(f.svar)+'</textarea></div>').join("");
        el.querySelectorAll(".fq-sp").forEach(x=>x.oninput=()=>{faqArb[+x.getAttribute("data-i")].sp=x.value;});
        el.querySelectorAll(".fq-svar").forEach(x=>x.oninput=()=>{faqArb[+x.getAttribute("data-i")].svar=x.value;});
        el.querySelectorAll(".fq-op").forEach(b=>b.onclick=()=>{const i=+b.getAttribute("data-i");if(i>0){const t=faqArb[i-1];faqArb[i-1]=faqArb[i];faqArb[i]=t;faqEditTegn();}});
        el.querySelectorAll(".fq-ned").forEach(b=>b.onclick=()=>{const i=+b.getAttribute("data-i");if(i<faqArb.length-1){const t=faqArb[i+1];faqArb[i+1]=faqArb[i];faqArb[i]=t;faqEditTegn();}});
        el.querySelectorAll(".fq-slet").forEach(b=>b.onclick=()=>{if(confirm("Slet dette spørgsmål?")){faqArb.splice(+b.getAttribute("data-i"),1);faqEditTegn();}});
    }
    faqEditTegn();
    document.getElementById("faqTilf").onclick=()=>{faqArb.push({sp:"",svar:""});faqEditTegn();};
    document.getElementById("faqNulstil").onclick=()=>{if(confirm("Nulstil FAQ til standard-spørgsmålene?")){faqArb=FAQ_STD.map(f=>({sp:f.sp,svar:f.svar}));faqEditTegn();}};
    document.getElementById("tkGem").onclick=async function(){
        const blokke={};
        boks.querySelectorAll(".tk-blok").forEach(t=>{const v=t.value.trim();if(v!=="")blokke[t.getAttribute("data-id")]=v;});
        const faqUd=faqArb.filter(f=>(f.sp||"").trim()!==""||(f.svar||"").trim()!=="");
        this.disabled=true;const st=document.getElementById("tkStatus");
        try{const r=await apiKald({a:"tekstergem",token:sess.token,blokke:blokke,faq:faqUd});
            if(r&&r.ok){st.textContent="✔ Gemt "+new Date().toLocaleTimeString("da-DK",{hour:"2-digit",minute:"2-digit"})+" — ændringerne er live";TEKSTER={blokke:blokke,faq:faqUd};}
            else{st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        this.disabled=false;
    };
}

/* ===================== 🆘 Support (admin) ===================== */
var supAdmSag=null,supAdmSager=[],supAdmMig="",supFilter={q:"",status:"",rute:"",bruger:""};
async function renderSupportAdm(){
    const panel=document.getElementById("supPanel"),sess=fbaSess();
    if(!panel||!sess)return;
    if(supAdmSag){supAdmVisSag(supAdmSag);return;}
    panel.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    let j=null;try{j=await apiKald({a:"sadmin",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){panel.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
    supAdmSager=j.sager||[];supAdmMig=j.mig;
    const ruteSet={};supAdmSager.forEach(s=>{if(s.rute)ruteSet[s.rute]={ikon:s.ruteIkon,navn:s.ruteNavn};});
    const brugerSet={};supAdmSager.forEach(s=>{if(s.tildelt)brugerSet[s.tildelt]=s.tildeltVis||s.tildelt;});
    const stOpt=[["","Alle sager"],["aabne","Kun åbne"],["ufordelt","Ufordelte"],["mine","Mine"],["lukkede","Lukkede/håndterede"]];
    let h='<div class="fba-bar" style="margin:0 0 10px"><b style="font-size:1.05rem">💬 Beskeder &amp; support</b>'+
        '<button id="supSkrivKnap" style="'+PROG_KNAP+';margin-left:auto;background:var(--groen);border:0;color:#0d2a12">✍️ Skriv til en borger</button>'+
        (j.ret_tid?'<button id="supCfgKnap" style="'+PROG_KNAP+'">⚙️ Eskaleringstider</button>':'')+'</div><div id="supCfgBoks"></div>';
    h+='<div class="fba-kort" style="box-shadow:none;border:1px solid rgba(30,55,76,.12);padding:9px 11px;margin-bottom:10px"><div style="display:flex;gap:7px;flex-wrap:wrap;align-items:center">'+
        '<input id="supFQ" type="search" value="'+fbaEsc(supFilter.q)+'" placeholder="🔍 Søg emne, borger eller nummer …" style="flex:1;min-width:170px;border:2px solid #dbe6ee;border-radius:10px;padding:8px 12px;font-family:var(--font);font-size:.88rem">'+
        '<select id="supFStatus" style="'+PROG_FELT+'">'+stOpt.map(o=>'<option value="'+o[0]+'"'+(supFilter.status===o[0]?" selected":"")+'>'+o[1]+'</option>').join("")+'</select>'+
        (Object.keys(ruteSet).length>1?'<select id="supFRute" style="'+PROG_FELT+'"><option value="">Alle modtagere</option>'+Object.keys(ruteSet).map(rk=>'<option value="'+fbaEsc(rk)+'"'+(supFilter.rute===rk?" selected":"")+'>'+fbaEsc(ruteSet[rk].ikon+" "+ruteSet[rk].navn)+'</option>').join("")+'</select>':'')+
        (Object.keys(brugerSet).length?'<select id="supFBruger" style="'+PROG_FELT+'"><option value="">Alle medarbejdere</option>'+Object.keys(brugerSet).sort((a,b)=>String(brugerSet[a]).localeCompare(String(brugerSet[b]),'da')).map(bl=>'<option value="'+fbaEsc(bl)+'"'+(supFilter.bruger===bl?" selected":"")+'>👤 '+fbaEsc(brugerSet[bl])+'</option>').join("")+'</select>':'')+
        '<button id="supFRyd" style="'+PROG_KNAP+';color:var(--graa)">Ryd</button></div></div>';
    h+='<div id="supResultat"></div>';
    panel.innerHTML=h;
    const cfgK=document.getElementById("supCfgKnap");if(cfgK)cfgK.onclick=supCfgVis;
    const skK=document.getElementById("supSkrivKnap");if(skK)skK.onclick=supSkrivForm;
    const fq=document.getElementById("supFQ");if(fq){fq.oninput=()=>{supFilter.q=fq.value;supAdmFyld();};}
    const fs=document.getElementById("supFStatus");if(fs)fs.onchange=()=>{supFilter.status=fs.value;supAdmFyld();};
    const fr=document.getElementById("supFRute");if(fr)fr.onchange=()=>{supFilter.rute=fr.value;supAdmFyld();};
    const fb=document.getElementById("supFBruger");if(fb)fb.onchange=()=>{supFilter.bruger=fb.value;supAdmFyld();};
    const fry=document.getElementById("supFRyd");if(fry)fry.onclick=()=>{supFilter={q:"",status:"",rute:"",bruger:""};renderSupportAdm();};
    supAdmFyld();
}
function supAdmKort(s){return '<div class="fba-kort sup-adm-sag" data-sid="'+fbaEsc(s.sid)+'" style="cursor:pointer'+(s.over?';border-left:5px solid var(--roed)':(s.ubesvaret?';border-left:5px solid #f5b400':''))+'">'+
    '<div class="fba-top"><span style="font-weight:800">'+(s.over?'🔴 ':'')+fbaEsc(s.emne)+'</span>'+
    '<span data-bku="'+fbaEsc(s.u)+'" style="color:var(--blaa);font-size:.8rem;font-weight:700;cursor:pointer">'+fbaEsc(/^20\d{4}$/.test(String(s.u))?(s.u+(s.navn&&s.navn!==s.u?" · "+s.navn:"")):(s.navn||s.u))+'</span>'+
    '<span style="color:var(--graa);font-size:.78rem">'+fbaEsc(s.ruteIkon+" "+s.ruteNavn)+'</span>'+
    '<span style="margin-left:auto;font-size:.78rem;color:var(--graa)">'+(s.tildeltVis?'👤 '+fbaEsc(s.tildeltVis):(s.ubesvaret?'⏳ ufordelt':''))+' · '+fbaDato(s.sidst)+'</span></div>'+
    (s.over?'<div style="color:var(--roed);font-size:.74rem;font-weight:700;margin-top:2px">⏰ Over grænsen — synlig for alle medarbejdere</div>':'')+'</div>';}
function supAdmFyld(){
    const el=document.getElementById("supResultat");if(!el)return;
    const f=supFilter,mig=supAdmMig,sager=supAdmSager;
    const aktiv=!!(f.q||f.status||f.rute||f.bruger);
    const wire=()=>{el.querySelectorAll(".sup-adm-sag").forEach(k=>k.onclick=(e)=>{if(e.target&&e.target.getAttribute&&e.target.getAttribute("data-bku"))return;supAdmSag=k.getAttribute("data-sid");renderSupportAdm();});
        el.querySelectorAll("[data-bku]").forEach(x=>x.onclick=(e)=>{e.stopPropagation();if(typeof aabnBorgerKort==="function")aabnBorgerKort(x.getAttribute("data-bku"));});};
    if(!aktiv){
        const tilMig=sager.filter(s=>s.tildelt===mig&&!s.admLukket);
        const ufordelt=sager.filter(s=>!s.tildelt&&s.forMig&&!s.admLukket);
        const andre=sager.filter(s=>s.tildelt&&s.tildelt!==mig&&!s.admLukket);
        const haandt=sager.filter(s=>s.admLukket);
        const H=(t,liste)=>liste.length?'<h4 class="adm-sek-h">'+t+' <span style="color:var(--graa);font-weight:400;font-size:.8rem">'+liste.length+'</span></h4>'+liste.map(supAdmKort).join(""):'';
        let h=(ufordelt.length?H("🔴 Kræver handling (ufordelte)",ufordelt):'<p style="color:var(--graa)">Ingen ufordelte sager til dig lige nu 🎉</p>');
        h+=H("👤 Mine sager",tilMig)+H("💬 I gang hos andre",andre);
        if(haandt.length)h+='<details class="tf-fold" style="margin-top:12px"><summary class="tf-sum">✔ '+haandt.length+' håndterede</summary><div style="padding:8px 0">'+haandt.map(supAdmKort).join("")+'</div></details>';
        el.innerHTML=h;wire();return;
    }
    let liste=sager.slice();
    if(f.status==="aabne")liste=liste.filter(s=>!s.admLukket);
    else if(f.status==="lukkede")liste=liste.filter(s=>s.admLukket);
    else if(f.status==="ufordelt")liste=liste.filter(s=>!s.tildelt&&!s.admLukket);
    else if(f.status==="mine")liste=liste.filter(s=>s.tildelt===mig&&!s.admLukket);
    if(f.rute)liste=liste.filter(s=>s.rute===f.rute);
    if(f.bruger)liste=liste.filter(s=>s.tildelt===f.bruger);
    if(f.q){const q=f.q.toLowerCase();liste=liste.filter(s=>(s.emne||"").toLowerCase().indexOf(q)>=0||(s.navn||"").toLowerCase().indexOf(q)>=0||String(s.u).toLowerCase().indexOf(q)>=0);}
    liste.sort((a,b)=>(b.sidst||0)-(a.sidst||0));
    el.innerHTML='<h4 class="adm-sek-h">Resultat <span style="color:var(--graa);font-weight:400;font-size:.8rem">'+liste.length+'</span></h4>'+(liste.length?liste.map(supAdmKort).join(""):'<p style="color:var(--graa)">Ingen sager matcher filteret.</p>');
    wire();
}
function supSkrivForm(){
    const panel=document.getElementById("supPanel");if(!panel)return;
    const borgere=(typeof adgangsBorgere==="function")?adgangsBorgere(true):[];
    panel.innerHTML='<button id="supSkrivRetur" style="'+PROG_KNAP+';margin:0 0 12px">‹ Beskeder & support</button>'+
        '<div class="fba-kort"><b style="font-size:1.05rem">✍️ Skriv til en borger</b>'+
        '<input id="supBSoeg" type="search" placeholder="🔍 Søg borger på navn eller nummer …" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:10px 14px;font-family:var(--font);font-size:.92rem;margin-top:10px">'+
        '<div id="supBListe" style="max-height:190px;overflow-y:auto;margin-top:6px"></div>'+
        '<div id="supBValgt" style="margin-top:8px"></div>'+
        '<input id="supSkEmne" maxlength="140" placeholder="Emne" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:10px 14px;font-family:var(--font);font-size:.92rem;margin-top:10px">'+
        '<textarea id="supSkTxt" rows="4" maxlength="4000" placeholder="Besked til borgeren …" style="width:100%;box-sizing:border-box;border:2px solid #dbe6ee;border-radius:12px;padding:10px 14px;font-family:var(--font);font-size:.92rem;margin-top:8px"></textarea>'+
        '<div id="supSkFiler"></div>'+
        '<div style="display:flex;gap:10px;align-items:center;margin-top:10px;flex-wrap:wrap"><button id="supSkSend" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">Send beskeden</button><button id="supSkVedh" type="button" style="'+PROG_KNAP+';background:#eef3f7;color:var(--moerk);border:0">📎 Vedhæft</button><span id="supSkFejl" style="color:var(--roed);font-size:.85rem"></span></div></div>';
    document.getElementById("supSkrivRetur").onclick=()=>{renderSupportAdm();};
    const skUp=supUploader(document.getElementById("supSkFiler"),{token:(fbaSess()||{}).token});
    document.getElementById("supSkVedh").onclick=()=>skUp.vaelg();
    var valgtU="",valgtNavn="";
    const soeg=document.getElementById("supBSoeg"),liste=document.getElementById("supBListe"),valgtEl=document.getElementById("supBValgt");
    function tegnListe(){
        const q=(soeg.value||"").trim().toLowerCase();
        if(q.length<1){liste.innerHTML='<p style="color:var(--graa);font-size:.82rem">Skriv navn eller nummer for at søge …</p>';return;}
        const m=borgere.filter(b=>String(b.u).toLowerCase().indexOf(q)>=0||String(b.n||"").toLowerCase().indexOf(q)>=0).slice(0,40);
        liste.innerHTML=m.length?m.map(b=>'<div class="sup-b-valg" data-u="'+fbaEsc(b.u)+'" data-n="'+fbaEsc(b.n||"")+'" style="padding:8px 11px;border-radius:8px;cursor:pointer;font-size:.88rem;border:1px solid rgba(30,55,76,.08);margin-bottom:4px"><b>'+fbaEsc(b.n||b.u)+'</b> <span style="color:var(--graa)">· '+fbaEsc(b.u)+'</span></div>').join(""):'<p style="color:var(--graa);font-size:.82rem">Ingen borgere matcher</p>';
        liste.querySelectorAll(".sup-b-valg").forEach(x=>x.onclick=()=>{valgtU=x.getAttribute("data-u");valgtNavn=x.getAttribute("data-n");valgtEl.innerHTML='<span style="background:rgba(60,180,229,.14);color:#1f7ea8;border-radius:999px;padding:5px 13px;font-weight:700;font-size:.85rem">✓ '+fbaEsc((valgtNavn||valgtU)+" · "+valgtU)+'</span>';liste.innerHTML="";soeg.value="";});
    }
    soeg.oninput=tegnListe;tegnListe();
    document.getElementById("supSkSend").onclick=async function(){
        const emne=document.getElementById("supSkEmne").value.trim(),txt=document.getElementById("supSkTxt").value.trim(),fe=document.getElementById("supSkFejl"),sess=fbaSess();
        if(!valgtU){fe.textContent="Vælg en borger først.";return;}
        if(!emne||!txt){fe.textContent="Udfyld emne og besked.";return;}
        this.disabled=true;fe.textContent="";
        try{const r=await apiKald({a:"sopretadm",token:sess.token,u:valgtU,emne:emne,txt:txt,filer:skUp.filer});
            if(r&&r.ok){supAdmSag=r.sid;renderSupportAdm();}else{fe.textContent=(r&&r.fejl)||"Kunne ikke sende.";this.disabled=false;}}
        catch(e){fe.textContent="Kunne ikke sende — tjek forbindelsen.";this.disabled=false;}
    };
}
async function supAdmVisSag(sid){
    const panel=document.getElementById("supPanel"),sess=fbaSess();
    if(!panel||!sess)return;
    let j=null;try{j=await apiKald({a:"shentadm",token:sess.token,sid:sid});}catch(e){}
    if(!(j&&j.ok)){panel.innerHTML='<p style="color:var(--roed)">Kunne ikke hente sagen.</p>';return;}
    const mig=(fbaSess()||{}).u;
    const supTagH=t=>fbaEsc(t).replace(/@([A-Za-z0-9ÆØÅæøåÄÖäöü._-]+)/g,'<span style="color:var(--blaa);font-weight:700">@$1</span>');
    const bobler=(j.beskeder||[]).map(b=>{
        if(b.fra==="system")return '<div style="text-align:center;color:var(--graa);font-size:.78rem;margin:6px 0">'+fbaEsc(b.txt)+'</div>';
        if(b.fra==="intern"){
            const inavn=b.afVis||b.af||"Medarbejder";
            return '<div style="display:flex;gap:10px;margin:0 0 14px">'+
                hfAvatarHTML(b.af,inavn)+
                '<div style="flex:1;min-width:0;background:#fff8e1;border:1px solid #f2df9e;border-left:4px solid #e0a800;border-radius:12px;padding:8px 12px">'+
                '<div style="font-size:.68rem;color:#8a6d00;font-weight:800;text-transform:uppercase;letter-spacing:.03em;margin-bottom:2px">🔒 Intern note · kun medarbejdere</div>'+
                '<div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(inavn)+'</b> · '+fbaDato(b.ts)+'</div>'+
                (b.txt?'<div style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+supTagH(b.txt)+'</div>':'')+
                supFilerHTML(b.filer,false)+
                '</div></div>';
        }
        const adm=b.fra==="adm";
        const navn=b.afVis||b.af||(adm?"Medarbejder":"Borger");
        return '<div style="display:flex;gap:10px;margin:0 0 14px">'+
            hfAvatarHTML(b.af,navn)+
            '<div style="flex:1;min-width:0">'+
            '<div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(navn)+'</b> · '+fbaDato(b.ts)+'</div>'+
            (b.txt?'<div style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+fbaEsc(b.txt)+'</div>':'')+
            supFilerHTML(b.filer,false)+
            '</div></div>';
    }).join("");
    const vmap=(j&&j.vismap&&Object.keys(j.vismap).length)?j.vismap:((lcaData&&lcaData.vismap)?lcaData.vismap:((typeof ADM_VIS!=="undefined"&&ADM_VIS)?ADM_VIS:{}));
    const overdragOpts='<option value="">— overdrag til …</option>'+Object.keys(vmap).sort((a,b)=>String(vmap[a]).localeCompare(String(vmap[b]),'da')).map(l=>'<option value="'+fbaEsc(l)+'">'+fbaEsc(vmap[l])+'</option>').join("");
    panel.innerHTML='<button id="supAdmRetur" style="'+PROG_KNAP+';margin:0 0 12px">‹ Support-kø</button>'+
        '<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="padding:14px 18px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;display:flex;gap:10px;align-items:center;flex-wrap:wrap">'+
        '<div style="flex:1;min-width:180px"><b>'+fbaEsc(j.emne)+'</b><div style="opacity:.8;font-size:.78rem"><span data-bku="'+fbaEsc(j.u)+'" style="cursor:pointer;text-decoration:underline">'+fbaEsc(/^20\d{4}$/.test(String(j.u))?(j.u+(j.navn&&j.navn!==j.u?" · "+j.navn:"")):(j.navn||j.u))+'</span> · '+fbaEsc(j.ruteNavn)+(j.tildeltVis?' · 👤 '+fbaEsc(j.tildeltVis):' · ufordelt')+'</div></div>'+
        (j.tildelt!==mig?'<button id="supTag" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">✋ Tag sagen</button>':'')+
        '<div style="position:relative"><input id="supOverdragSoeg" placeholder="↪ Overdrag til …" autocomplete="off" style="'+PROG_FELT+';max-width:180px"><div id="supOverdragListe" style="position:absolute;z-index:30;top:calc(100% + 3px);right:0;min-width:210px;background:#fff;color:var(--moerk);border:1px solid rgba(30,55,76,.2);border-radius:10px;max-height:230px;overflow:auto;display:none;box-shadow:0 8px 24px rgba(0,0,0,.16)"></div></div>'+
        '<button id="supAdmLuk" style="'+PROG_KNAP+'">'+(j.admLukket?'↩ Genåbn':'✔ Luk')+'</button></div>'+
        '<div style="display:flex;flex-direction:column;gap:2px;padding:16px;background:var(--flade);max-height:50vh;overflow-y:auto">'+bobler+'</div>'+
        '<div style="padding:12px 16px;background:#fff;border-top:1px solid rgba(30,55,76,.08)"><div style="display:flex;gap:10px;align-items:flex-start"><button id="supSvarVedh" type="button" title="Vedhæft billede eller fil" style="border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:10px;width:42px;height:42px;font-size:1.05rem;cursor:pointer;flex:0 0 auto">📎</button><textarea id="supSvarTxt" rows="2" placeholder="Skriv et svar til borgeren …" style="flex:1;border:1px solid rgba(30,55,76,.16);border-radius:11px;padding:9px 12px;font-family:var(--font);font-size:.9rem;resize:vertical"></textarea>'+hfEmojiKnap("supSvarTxt")+'<button id="supSvarKnap" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">Svar ➤</button></div><div id="supAdmSvarFiler"></div></div>'+
        '<div style="padding:12px 16px;background:#fffdf5;border-top:1px solid #f2df9e">'+
        '<div style="font-size:.74rem;color:#8a6d00;font-weight:800;margin-bottom:6px">🔒 Intern note (dokumentation på sagen) — borgeren ser den ALDRIG. Tag en kollega med @navn.</div>'+
        '<div style="display:flex;gap:10px;align-items:flex-start;position:relative">'+
        '<button id="supNoteVedh" type="button" title="Vedhæft fil til noten" style="border:1px solid #e6d59a;background:#fff;border-radius:10px;width:42px;height:42px;font-size:1.05rem;cursor:pointer;flex:0 0 auto">📎</button>'+
        '<div style="flex:1;position:relative"><textarea id="supNoteTxt" rows="2" placeholder="Skriv en intern note … brug @navn for at tagge en kollega" style="width:100%;border:1px solid #e6d59a;border-radius:11px;padding:9px 12px;font-family:var(--font);font-size:.9rem;resize:vertical;background:#fffef9"></textarea>'+
        '<div id="supNoteTag" style="position:absolute;z-index:30;left:0;right:0;bottom:calc(100% + 3px);background:#fff;color:var(--moerk);border:1px solid rgba(30,55,76,.2);border-radius:10px;max-height:180px;overflow:auto;display:none;box-shadow:0 8px 24px rgba(0,0,0,.16)"></div></div>'+
        hfEmojiKnap("supNoteTxt")+
        '<button id="supNoteKnap" style="'+PROG_KNAP+';background:#e0a800;color:#3a2e00;border:0">🔒 Gem note</button>'+
        '</div><div id="supAdmNoteFiler"></div>'+
        '</div></div>';
    hfAvatarWire(panel);
    document.getElementById("supAdmRetur").onclick=()=>{supAdmSag=null;renderSupportAdm();};
    const tg=document.getElementById("supTag");if(tg)tg.onclick=async()=>{await apiKald({a:"stag",token:sess.token,sid:sid}).catch(()=>{});supAdmVisSag(sid);};
    document.getElementById("supAdmLuk").onclick=async()=>{await apiKald({a:"sadmluk",token:sess.token,sid:sid,til:j.admLukket?0:1}).catch(()=>{});supAdmSag=null;renderSupportAdm();};
    (function(){
        const inp=document.getElementById("supOverdragSoeg"),liste=document.getElementById("supOverdragListe");
        if(!inp||!liste)return;
        const entries=Object.keys(vmap).filter(l=>l!==mig).map(l=>({l:l,n:vmap[l]||l})).sort((a,b)=>String(a.n).localeCompare(String(b.n),'da'));
        function tegn(){
            const q=(inp.value||"").trim().toLowerCase();
            const m=entries.filter(e=>String(e.n).toLowerCase().indexOf(q)>=0||String(e.l).toLowerCase().indexOf(q)>=0).slice(0,60);
            liste.innerHTML=m.length?m.map(e=>'<div class="sup-ov-valg" data-l="'+fbaEsc(e.l)+'" style="padding:8px 12px;cursor:pointer;font-size:.86rem;border-bottom:1px solid rgba(30,55,76,.06)">👤 '+fbaEsc(e.n)+'</div>').join(""):'<div style="padding:8px 12px;color:var(--graa);font-size:.82rem">Ingen medarbejder matcher</div>';
            liste.querySelectorAll(".sup-ov-valg").forEach(x=>x.onmousedown=async(ev)=>{ev.preventDefault();const til=x.getAttribute("data-l");liste.style.display="none";inp.value="";inp.blur();await apiKald({a:"soverdrag",token:sess.token,sid:sid,til:til}).catch(()=>{});supAdmVisSag(sid);});
        }
        inp.onfocus=()=>{tegn();liste.style.display="";};
        inp.oninput=()=>{tegn();liste.style.display="";};
        inp.onblur=()=>{setTimeout(()=>{liste.style.display="none";},180);};
    })();
    const sk=document.getElementById("supSvarKnap"),stx=document.getElementById("supSvarTxt"),svk=document.getElementById("supSvarVedh");
    const up=supUploader(document.getElementById("supAdmSvarFiler"),{token:sess.token});
    if(svk)svk.onclick=()=>up.vaelg();
    sk.onclick=async()=>{const v=(stx.value||"").trim();if(!v&&!up.filer.length)return;sk.disabled=true;
        try{const r=await apiKald({a:"sasvar",token:sess.token,sid:sid,txt:v,filer:up.filer});if(r&&r.ok){supAdmVisSag(sid);}else{sk.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}catch(e){sk.disabled=false;}};
    /* 🔒 intern note: uploader + @-autocomplete + gem via sanote */
    const noteBtn=document.getElementById("supNoteKnap"),ntx=document.getElementById("supNoteTxt"),nvk=document.getElementById("supNoteVedh");
    const nup=supUploader(document.getElementById("supAdmNoteFiler"),{token:sess.token});
    if(nvk)nvk.onclick=()=>nup.vaelg();
    (function(){
        const tag=document.getElementById("supNoteTag");if(!ntx||!tag)return;
        const folk=Object.keys(vmap).map(l=>({l:l,n:vmap[l]||l}));
        const luk=()=>{tag.style.display="none";};
        function tegnTag(){
            const val=ntx.value,pos=ntx.selectionStart||val.length,foer=val.slice(0,pos);
            const m=foer.match(/@([^\s@]*)$/);if(!m){luk();return;}
            const q=m[1].toLowerCase();
            const hit=folk.filter(f=>String(f.n).toLowerCase().indexOf(q)>=0||String(f.l).toLowerCase().indexOf(q)>=0).slice(0,30);
            if(!hit.length){luk();return;}
            tag.innerHTML=hit.map(f=>'<div class="sup-nt-valg" data-l="'+fbaEsc(f.l)+'" style="padding:8px 12px;cursor:pointer;font-size:.86rem;border-bottom:1px solid rgba(30,55,76,.06)">👤 '+fbaEsc(f.n)+' <span style="color:var(--graa);font-size:.76rem">@'+fbaEsc(f.l)+'</span></div>').join("");
            tag.style.display="";
            tag.querySelectorAll(".sup-nt-valg").forEach(x=>x.onmousedown=ev=>{ev.preventDefault();
                const login=x.getAttribute("data-l"),start=foer.replace(/@([^\s@]*)$/,"@"+login+" ");
                ntx.value=start+val.slice(pos);const np=start.length;ntx.focus();ntx.setSelectionRange(np,np);luk();});
        }
        ntx.addEventListener("input",tegnTag);
        ntx.addEventListener("keyup",e=>{if(e.key==="Escape")luk();});
        ntx.addEventListener("blur",()=>setTimeout(luk,180));
    })();
    if(noteBtn)noteBtn.onclick=async()=>{const v=(ntx.value||"").trim();if(!v&&!nup.filer.length)return;noteBtn.disabled=true;
        try{const r=await apiKald({a:"sanote",token:sess.token,sid:sid,txt:v,filer:nup.filer});if(r&&r.ok){supAdmVisSag(sid);}else{noteBtn.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme noten");}}catch(e){noteBtn.disabled=false;}};
    panel.querySelectorAll("[data-bku]").forEach(x=>x.onclick=()=>{if(typeof aabnBorgerKort==="function")aabnBorgerKort(x.getAttribute("data-bku"));});
}
async function supCfgVis(){
    const boks=document.getElementById("supCfgBoks"),sess=fbaSess();if(!boks||!sess)return;
    if(boks.dataset.open==="1"){boks.dataset.open="";boks.innerHTML="";return;}
    boks.dataset.open="1";boks.innerHTML='<p style="color:var(--graa)">⏳</p>';
    let j=null;try{j=await apiKald({a:"supcfghent",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente</p>';return;}
    const ruter=j.ruter||{},cfg=j.cfg||{};
    let h='<div class="fba-kort" style="border-left:5px solid #8a6400"><b>⚙️ Eskaleringstider</b>'+
        '<p style="color:var(--graa);font-size:.8rem">Minutter før en ubesvaret sag bliver synlig for næste trin.</p>';
    Object.keys(ruter).forEach(rk=>{
        const r=ruter[rk];h+='<div style="margin-top:8px"><b style="font-size:.88rem">'+r.ikon+' '+fbaEsc(r.navn)+'</b>';
        (r.trin||[]).forEach((t,i)=>{
            const maal=t.maal==="alle"?"alle medarbejdere":(t.maal.indexOf("gruppe:")===0?t.maal.slice(7)+"-gruppen":"tildelt konsulent");
            const v=(cfg[rk]&&cfg[rk][i]!==undefined)?cfg[rk][i]:t.min;
            h+='<div style="display:flex;gap:8px;align-items:center;margin-top:4px;font-size:.83rem"><span style="color:var(--graa);flex:1">Trin '+(i+1)+': → '+fbaEsc(maal)+'</span>'+
                (j.ret?'<input class="sup-cfg-min" data-r="'+rk+'" data-i="'+i+'" type="number" min="0" value="'+v+'" style="width:66px;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:5px"'+(i===0?' disabled':'')+'> min':'<b>'+v+' min</b>')+'</div>';
        });
        h+='</div>';
    });
    if(j.ret)h+='<div style="margin-top:10px"><button id="supCfgGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem tider</button> <span id="supCfgStatus" style="color:var(--graa);font-size:.82rem"></span></div>';
    else h+='<p style="color:var(--graa);font-size:.8rem;margin-top:8px">Kun ejer + direktion kan ændre tiderne.</p>';
    boks.innerHTML=h+'</div>';
    const gem=document.getElementById("supCfgGem");
    if(gem)gem.onclick=async function(){
        const cfgNy={};
        boks.querySelectorAll(".sup-cfg-min").forEach(inp=>{const r=inp.getAttribute("data-r"),i=+inp.getAttribute("data-i");if(!cfgNy[r])cfgNy[r]={};cfgNy[r][i]=parseInt(inp.value,10)||0;});
        this.disabled=true;const st=document.getElementById("supCfgStatus");
        try{const rr=await apiKald({a:"supcfggem",token:sess.token,cfg:cfgNy});if(rr&&rr.ok){st.textContent="✔ Gemt";}else{st.textContent="";alert((rr&&rr.fejl)||"Kunne ikke gemme");}}catch(e){alert("Kunne ikke gemme");}
        this.disabled=false;
    };
}

/* ===================== 🏷️ Grupper — organisatoriske tag-grupper ===================== */
var grpModel=null,grpVisning="liste",grpErValg="",grpMedValg="";
/* ===================== 📈 DASHBOARD — tilstedeværelse + aktivitet ===================== */
var dashData=null,dashPeriode="idag",dashSog="",dashTimer=null;
var DASH_PER=[["idag","I dag"],["uge","Denne uge"],["mnd","Denne måned"],["total","I alt"]];
function dashCSS(){
    if(document.getElementById("dashCSStag"))return;
    var s=document.createElement("style");s.id="dashCSStag";
    s.textContent=".dash{max-width:1120px}"+
        ".dash-top{display:flex;justify-content:space-between;align-items:flex-end;gap:16px;flex-wrap:wrap;margin-bottom:16px}"+
        ".dash-h{margin:0;font-size:1.5rem;color:var(--moerk);font-weight:800}"+
        ".dash-sub{color:var(--graa);font-size:.82rem;margin-top:3px}"+
        ".dash-actions{display:flex;gap:10px;align-items:center;flex-wrap:wrap}"+
        ".dash-per{display:inline-flex;background:#eef3f7;border-radius:12px;padding:3px;gap:2px}"+
        ".dash-per button{border:0;background:transparent;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;color:#2a3a48}"+
        ".dash-per button.aktiv{background:var(--moerk);color:#fff}"+
        ".dash-btn{border:0;border-radius:11px;padding:8px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;background:var(--blaa);color:#fff}"+
        ".dash-grid2{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-bottom:14px}"+
        ".dash-card{background:var(--hvid);border:1px solid #e6eef4;border-radius:16px;padding:16px 18px;box-shadow:var(--skygge);margin-bottom:14px}"+
        ".dash-card-h{font-weight:800;color:var(--moerk);font-size:1rem;display:flex;align-items:center;gap:8px;margin-bottom:12px}"+
        ".dash-card-h .lys{color:var(--graa);font-weight:400;font-size:.8rem}"+
        ".dash-badge{background:var(--groen);color:#123;border-radius:20px;padding:1px 10px;font-size:.8rem;font-weight:800}"+
        ".dash-badge.nul{background:#e6eef4;color:var(--graa)}"+
        ".dash-dot{width:9px;height:9px;border-radius:50%;background:var(--groen);box-shadow:0 0 0 4px rgba(147,213,0,.18);display:inline-block;flex:none}"+
        ".dash-onl{display:flex;flex-direction:column;gap:7px}"+
        ".dash-onl-row{display:flex;align-items:center;gap:9px;padding:7px 10px;background:var(--flade);border-radius:10px}"+
        ".dash-onl-navn{font-weight:600;color:var(--moerk);flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}"+
        ".dash-onl-tid{font-size:.78rem;color:var(--graa);font-weight:600;flex:none}"+
        ".dash-tom{color:var(--graa);font-size:.86rem;padding:8px 2px}"+
        ".dash-stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-bottom:16px}"+
        ".dash-stat{background:var(--hvid);border:1px solid #e6eef4;border-radius:16px;padding:14px 16px;box-shadow:var(--skygge)}"+
        ".dash-stat-ik{width:34px;height:34px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:1.05rem;margin-bottom:8px}"+
        ".dash-stat-tal{font-size:1.7rem;font-weight:800;color:var(--moerk);line-height:1}"+
        ".dash-stat-lbl{color:var(--moerk);font-weight:700;font-size:.84rem;margin-top:3px}"+
        ".dash-stat-sub{color:var(--graa);font-size:.74rem;margin-top:2px}"+
        ".dash-sog{width:100%;box-sizing:border-box;border:1px solid #dbe6ee;border-radius:12px;padding:11px 14px;font-family:var(--font);font-size:.9rem;margin-bottom:14px;outline:none}"+
        ".dash-tabel{width:100%;border-collapse:collapse;font-size:.88rem}"+
        ".dash-tabel th{text-align:right;color:var(--graa);font-size:.72rem;text-transform:uppercase;letter-spacing:.04em;padding:6px 10px;border-bottom:2px solid #eef3f7;white-space:nowrap}"+
        ".dash-tabel th:first-child,.dash-tabel td:first-child{text-align:left}"+
        ".dash-tabel td{padding:8px 10px;border-bottom:1px solid #f0f5f9;text-align:right;color:var(--moerk)}"+
        ".dash-tabel .dash-navn{font-weight:600}"+
        ".dash-tabel .dash-sum{font-weight:800}"+
        ".dash-tabel tr.dash-total td{background:var(--flade);font-weight:800;border-bottom:2px solid #e6eef4}"+
        "@media(max-width:720px){.dash-grid2{grid-template-columns:1fr}}";
    document.head.appendChild(s);
}
function dashVarighed(min){min=parseInt(min,10)||0;if(min<1)return "netop nu";if(min<60)return min+" min";var t=Math.floor(min/60),m=min%60;return t+"t"+(m?" "+m+"m":"");}
function dashV(b){return (b&&b[dashPeriode]!=null)?b[dashPeriode]:0;}
function dashTot(b){return (b&&b.total!=null)?b.total:0;}
function dashPerNavn(){for(var i=0;i<DASH_PER.length;i++){if(DASH_PER[i][0]===dashPeriode)return DASH_PER[i][1];}return "";}
async function dashHent(silent){
    const sess=fbaSess();const wrap=document.getElementById("dashWrap");if(!wrap)return;
    if(!sess){wrap.innerHTML='<p style="color:var(--roed)">Kræver login.</p>';return;}
    if(!silent&&!dashData)wrap.innerHTML='<p style="color:var(--graa);padding:20px">⏳ Henter dashboard…</p>';
    let j;try{j=await apiKald({a:"dashboard",token:sess.token});}catch(e){j=null;}
    if(!j||!j.ok){if(!dashData)wrap.innerHTML='<p style="color:var(--roed)">Kunne ikke hente dashboardet'+(j&&j.fejl?': '+fbaEsc(j.fejl):'')+'.</p>';return;}
    dashData=j;dashTegn();
}
function renderDashboard(){dashCSS();dashHent(false);dashAutoStart();}
function dashAutoStart(){
    if(dashTimer)clearInterval(dashTimer);
    dashTimer=setInterval(function(){
        if(admOmr!=="dashboard"){clearInterval(dashTimer);dashTimer=null;return;}
        if(document.hidden)return;
        dashHent(true);
    },30000);
}
function dashOnlListe(arr){
    if(!arr||!arr.length)return '<div class="dash-tom">Ingen online lige nu</div>';
    return '<div class="dash-onl">'+arr.map(function(p){
        var n=p.navn||p.uid||"—";
        return '<div class="dash-onl-row"><span class="dash-dot"></span><span class="dash-onl-navn">'+fbaEsc(n)+'</span><span class="dash-onl-tid">i '+dashVarighed(p.minutter)+'</span></div>';
    }).join("")+'</div>';
}
function dashStat(ik,label,b,farve){
    return '<div class="dash-stat"><div class="dash-stat-ik" style="background:'+farve+'22;color:'+farve+'">'+ik+'</div>'+
        '<div class="dash-stat-tal">'+dashV(b)+'</div><div class="dash-stat-lbl">'+label+'</div>'+
        '<div class="dash-stat-sub">'+dashTot(b)+' i alt</div></div>';
}
function dashTegn(){
    var wrap=document.getElementById("dashWrap");if(!wrap||!dashData)return;
    var d=dashData,q=dashSog.trim().toLowerCase();
    var on=d.online||{adm:[],borgere:[],antalAdm:0,antalBorgere:0};
    var team=d.team||{beskeder:{},chats:{},afsluttet:{}};
    var bor=d.borger||{chats:{},beskeder:{},top:[]};
    var perKnap=DASH_PER.map(function(p){return '<button data-p="'+p[0]+'"'+(dashPeriode===p[0]?' class="aktiv"':'')+'>'+p[1]+'</button>';}).join("");
    var tid=new Date();var tidStr=("0"+tid.getHours()).slice(-2)+":"+("0"+tid.getMinutes()).slice(-2);
    var h='<div class="dash">'+
        '<div class="dash-top"><div>'+
        '<h2 class="dash-h">📈 Dashboard</h2>'+
        '<div class="dash-sub">Live overblik · opdateret kl. '+tidStr+' · fornyer sig selv hvert ½ minut</div>'+
        '</div><div class="dash-actions">'+
        '<div class="dash-per">'+perKnap+'</div>'+
        '<button id="dashOpd" class="dash-btn">↻ Opdatér</button>'+
        '</div></div>'+
        /* ONLINE NU */
        '<div class="dash-grid2">'+
        '<div class="dash-card"><div class="dash-card-h"><span class="dash-dot"></span> Medarbejdere online <span class="dash-badge'+(on.antalAdm?'':' nul')+'">'+on.antalAdm+'</span></div>'+dashOnlListe(on.adm)+'</div>'+
        '<div class="dash-card"><div class="dash-card-h"><span class="dash-dot"></span> Borgere online <span class="dash-badge'+(on.antalBorgere?'':' nul')+'">'+on.antalBorgere+'</span></div>'+dashOnlListe(on.borgere)+'</div>'+
        '</div>'+
        /* NØGLETAL FOR PERIODEN */
        '<div class="dash-stats">'+
        dashStat("💬","Chats · borgere",bor.chats,"#3cb4e5")+
        dashStat("📨","Beskeder · borgere",bor.beskeder,"#93d500")+
        dashStat("📬","Beskedsvar · team",team.beskeder,"#e7004c")+
        dashStat("🗂","Chat-håndteringer · team",team.chats,"#1e374c")+
        dashStat("✅","Afsluttede · team",team.afsluttet,"#3cb4e5")+
        '</div>'+
        '<input id="dashSog" class="dash-sog" placeholder="🔎 Søg medarbejder eller borger…" value="'+fbaEsc(dashSog)+'">';
    /* MEDARBEJDER-AKTIVITET */
    var med=(d.medarb||[]).filter(function(m){return !q||String(m.navn||"").toLowerCase().indexOf(q)>=0||String(m.login||"").toLowerCase().indexOf(q)>=0;});
    var tb=dashV(team.beskeder),tc=dashV(team.chats),ta=dashV(team.afsluttet);
    h+='<div class="dash-card"><div class="dash-card-h">🧑‍💼 Medarbejder-aktivitet <span class="lys">· '+fbaEsc(dashPerNavn().toLowerCase())+'</span></div>'+
        '<table class="dash-tabel"><thead><tr><th>Medarbejder</th><th>📬 Beskedsvar</th><th>🗂 Chat-håndt.</th><th>✅ Afsluttet</th><th>Σ</th></tr></thead><tbody>'+
        '<tr class="dash-total"><td>Hele teamet</td><td>'+tb+'</td><td>'+tc+'</td><td>'+ta+'</td><td class="dash-sum">'+(tb+tc+ta)+'</td></tr>';
    if(!med.length)h+='<tr><td colspan="5" style="color:var(--graa)">Ingen medarbejdere med aktivitet'+(q?' i søgningen':'')+'.</td></tr>';
    else med.forEach(function(m){
        var b=dashV(m.beskeder),c=dashV(m.chats),a=dashV(m.afsluttet);
        h+='<tr><td class="dash-navn">'+fbaEsc(m.navn||m.login)+'</td><td>'+b+'</td><td>'+c+'</td><td>'+a+'</td><td class="dash-sum">'+(b+c+a)+'</td></tr>';
    });
    h+='</tbody></table></div>';
    /* MEST AKTIVE BORGERE (samlet forbrug) */
    var top=(bor.top||[]).filter(function(x){return !q||String(x.navn||"").toLowerCase().indexOf(q)>=0||String(x.u||"").indexOf(q)>=0;});
    h+='<div class="dash-card"><div class="dash-card-h">👥 Mest aktive borgere <span class="lys">· samlet forbrug</span></div>'+
        '<table class="dash-tabel"><thead><tr><th>Borger</th><th>💬 Chats</th><th>📨 Beskeder</th><th>Σ</th></tr></thead><tbody>';
    if(!top.length)h+='<tr><td colspan="4" style="color:var(--graa)">Ingen borger-aktivitet'+(q?' i søgningen':'')+' endnu.</td></tr>';
    else top.forEach(function(x){
        var navn=x.navn?x.navn+" ("+x.u+")":x.u;
        h+='<tr><td class="dash-navn">'+fbaEsc(navn)+'</td><td>'+(x.chats||0)+'</td><td>'+(x.beskeder||0)+'</td><td class="dash-sum">'+((x.chats||0)+(x.beskeder||0))+'</td></tr>';
    });
    h+='</tbody></table></div></div>';
    wrap.innerHTML=h;
    /* wiring */
    wrap.querySelectorAll(".dash-per button").forEach(function(b){b.onclick=function(){dashPeriode=b.getAttribute("data-p");dashTegn();};});
    var opd=document.getElementById("dashOpd");if(opd)opd.onclick=function(){dashHent(false);};
    var sog=document.getElementById("dashSog");
    if(sog)sog.oninput=function(){dashSog=sog.value;var pos=sog.selectionStart;dashTegn();var s2=document.getElementById("dashSog");if(s2){s2.focus();try{s2.setSelectionRange(pos,pos);}catch(e){}}};
}

/* ===================== 🔑 mitHF-adgang (SSO) — hvem må logge ind i mitHF ===================== */
var ssoData=null,ssoSoeg="",ssoFilter="alle",ssoValgt={};
function ssoCSS(){
    if(document.getElementById("ssoCSStag"))return;
    var s=document.createElement("style");s.id="ssoCSStag";
    s.textContent=".sso{max-width:1000px}"+
        ".sso-h{margin:0;font-size:1.5rem;color:var(--moerk);font-weight:800}"+
        ".sso-sub{color:var(--graa);font-size:.86rem;margin:4px 0 0;line-height:1.5}"+
        ".sso-kort{background:var(--hvid);border:1px solid #e6eef4;border-radius:16px;padding:14px 16px;box-shadow:var(--skygge);margin:14px 0}"+
        ".sso-knap{border:0;border-radius:11px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.84rem}"+
        ".sso-groen{background:var(--groen);color:#123}.sso-roed{background:var(--roed);color:#fff}.sso-graa{background:#eef3f7;color:#2a3a48}"+
        ".sso-row{display:flex;align-items:center;gap:11px;padding:9px 14px;border-bottom:1px solid #f0f5f9}"+
        ".sso-row:hover{background:var(--flade)}"+
        ".sso-navn{font-weight:600;color:var(--moerk);flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}"+
        ".sso-bad{font-size:.72rem;font-weight:800;border-radius:999px;padding:2px 9px;white-space:nowrap}"+
        ".sso-sog{width:100%;box-sizing:border-box;border:1px solid #dbe6ee;border-radius:12px;padding:10px 14px;font-family:var(--font);font-size:.9rem;margin:0 0 10px;outline:none}"+
        ".sso-filt button{border:0;background:#eef3f7;border-radius:999px;padding:6px 12px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem;color:#2a3a48;margin:0 6px 6px 0}"+
        ".sso-filt button.aktiv{background:var(--moerk);color:#fff}";
    document.head.appendChild(s);
}
function ssoStatusBad(st){
    if(st==="aktiv")return '<span class="sso-bad" style="background:rgba(147,213,0,.2);color:#41630a">🟢 Aktiv</span>';
    if(st==="lukket")return '<span class="sso-bad" style="background:rgba(231,0,76,.12);color:#a3003a">🔴 Lukket</span>';
    return '<span class="sso-bad" style="background:#eef3f7;color:var(--graa)">⚪ Ikke aktiv</span>';
}
async function renderSSOAdgang(){
    ssoCSS();
    var wrap=document.getElementById("ssoWrap");if(!wrap)return;
    var sess=fbaSess();if(!sess){wrap.innerHTML='<p style="color:var(--roed)">Kræver login.</p>';return;}
    if(!ssoData)wrap.innerHTML='<p style="color:var(--graa);padding:20px">⏳ Henter mitHF-adgang…</p>';
    var j;try{j=await apiKald({a:"ssoadgangliste",token:sess.token});}catch(e){j=null;}
    if(!j||!j.ok){wrap.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente mitHF-adgang.")+'</p>';return;}
    ssoData=j;ssoTegn();
}
function ssoTegn(){
    var wrap=document.getElementById("ssoWrap");if(!wrap||!ssoData)return;
    var d=ssoData,q=ssoSoeg.trim().toLowerCase();
    var liste=(d.liste||[]).filter(function(b){
        if(q&&String(b.navn||"").toLowerCase().indexOf(q)<0&&String(b.u||"").indexOf(q)<0)return false;
        if(ssoFilter==="aktiv")return b.status==="aktiv";
        if(ssoFilter==="lukket")return b.status==="lukket";
        if(ssoFilter==="mangler")return !b.harBid;
        return true;
    });
    var nValgt=Object.keys(ssoValgt).length;
    var h='<div class="sso">'+
        '<h2 class="sso-h">🔑 mitHF-adgang</h2>'+
        '<p class="sso-sub">Bestem hvem der kan logge ind i mitHF direkte fra universet. Borgere uden adgang ser «mitHF er på vej»-beskeden. En borger kan kun aktiveres, når den har et brugere-id.</p>'+
        (d.global?'':'<div class="sso-kort" style="border-left:5px solid var(--roed)"><b>⚠️ mitHF-login er slået fra globalt.</b> Aktiveringer her får først virkning, når det slås til i opsætningen.</div>')+
        '<div class="sso-kort" style="display:flex;flex-wrap:wrap;gap:10px;align-items:center">'+
        '<span class="sso-bad" style="background:rgba(147,213,0,.2);color:#41630a">🟢 '+d.nAktiv+' aktive</span>'+
        '<span class="sso-bad" style="background:rgba(231,0,76,.12);color:#a3003a">🔴 '+d.nLukket+' lukkede</span>'+
        '<span class="sso-bad" style="background:rgba(245,166,35,.18);color:#a86b00">⚠️ '+d.nMangler+' mangler brugere-id</span>'+
        '<span style="flex:1;min-width:20px"></span>'+
        '<button id="ssoAlleAktiv" class="sso-knap sso-groen">✅ Aktivér alle klar-borgere</button>'+
        '<button id="ssoAlleLuk" class="sso-knap sso-roed">⛔ Luk for alle</button>'+
        '</div>'+
        (nValgt?'<div class="sso-kort" style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;background:#eef7fb"><b>'+nValgt+' valgt</b><span style="flex:1;min-width:20px"></span>'+
            '<button id="ssoValgtAktiv" class="sso-knap sso-groen">Aktivér valgte ('+nValgt+')</button>'+
            '<button id="ssoValgtLuk" class="sso-knap sso-roed">Luk valgte ('+nValgt+')</button>'+
            '<button id="ssoRydValg" class="sso-knap sso-graa">Ryd valg</button></div>':'')+
        '<input id="ssoSog" class="sso-sog" placeholder="🔎 Søg navn eller kundenr…" value="'+fbaEsc(ssoSoeg)+'">'+
        '<div class="sso-filt">'+
        [["alle","Alle"],["aktiv","Aktive"],["lukket","Lukkede"],["mangler","Mangler brugere-id"]].map(function(f){return '<button data-f="'+f[0]+'"'+(ssoFilter===f[0]?' class="aktiv"':'')+'>'+f[1]+'</button>';}).join("")+
        '</div>'+
        '<div class="sso-kort" style="padding:2px 0">';
    if(!liste.length)h+='<p style="color:var(--graa);padding:14px">Ingen borgere'+(q||ssoFilter!=="alle"?" i filteret":"")+'.</p>';
    else liste.forEach(function(b){
        var bidBad=b.harBid?'<span class="sso-bad" style="background:rgba(60,180,229,.15);color:#1f7ea8">✅ brugere-id</span>':'<span class="sso-bad" style="background:rgba(245,166,35,.18);color:#a86b00">⚠️ mangler id</span>';
        var toggle=b.status==="aktiv"
            ?'<button class="sso-toggle sso-knap sso-roed" data-u="'+fbaEsc(b.u)+'" data-til="lukket" data-n="'+fbaEsc(b.navn||"")+'">Luk adgang</button>'
            :'<button class="sso-toggle sso-knap sso-groen" data-u="'+fbaEsc(b.u)+'" data-til="aktiv" data-n="'+fbaEsc(b.navn||"")+'"'+(b.harBid?'':' disabled style="opacity:.45;cursor:not-allowed"')+'>Aktivér</button>';
        h+='<div class="sso-row">'+
            '<input type="checkbox" class="sso-chk" data-u="'+fbaEsc(b.u)+'"'+(ssoValgt[b.u]?' checked':'')+'>'+
            '<span class="sso-navn">'+fbaEsc(b.navn||b.u)+' <span style="color:var(--graa);font-weight:400">('+fbaEsc(b.u)+')</span></span>'+
            bidBad+ssoStatusBad(b.status)+toggle+
            '</div>';
    });
    h+='</div></div>';
    wrap.innerHTML=h;
    var sog=document.getElementById("ssoSog");if(sog)sog.oninput=function(){ssoSoeg=sog.value;var p=sog.selectionStart;ssoTegn();var s2=document.getElementById("ssoSog");if(s2){s2.focus();try{s2.setSelectionRange(p,p);}catch(e){}}};
    wrap.querySelectorAll(".sso-filt button").forEach(function(b){b.onclick=function(){ssoFilter=b.getAttribute("data-f");ssoTegn();};});
    wrap.querySelectorAll(".sso-chk").forEach(function(c){c.onchange=function(){var u=c.getAttribute("data-u");if(c.checked)ssoValgt[u]=1;else delete ssoValgt[u];ssoTegn();};});
    wrap.querySelectorAll(".sso-toggle").forEach(function(b){b.onclick=function(){
        var u=b.getAttribute("data-u"),til=b.getAttribute("data-til"),nvn=b.getAttribute("data-n")||u;
        if(til==="aktiv"){if(!confirm("Giv "+nvn+" adgang til at logge ind i mitHF fra universet?"))return;}
        else{if(!confirm("Luk "+nvn+"s mitHF-adgang?\nBorgeren ser «mitHF er på vej»-beskeden i stedet."))return;}
        ssoSaet({u:u,til:til});
    };});
    var aa=document.getElementById("ssoAlleAktiv");if(aa)aa.onclick=function(){if(!confirm("Aktivér mitHF-adgang for ALLE borgere, der har et brugere-id og ikke er manuelt lukket?\n\nDet gælder alle borgere i universet."))return;ssoSaet({alle:1,til:"aktiv"});};
    var al=document.getElementById("ssoAlleLuk");if(al)al.onclick=function(){if(!confirm("Luk mitHF-adgang for ALLE borgere?\nAlle ser «mitHF er på vej»-beskeden."))return;ssoSaet({alle:1,til:"lukket"});};
    var va=document.getElementById("ssoValgtAktiv");if(va)va.onclick=function(){var us=Object.keys(ssoValgt);if(!us.length)return;if(!confirm("Aktivér mitHF-adgang for de "+us.length+" valgte borgere?"))return;ssoSaet({us:us,til:"aktiv"});};
    var vl=document.getElementById("ssoValgtLuk");if(vl)vl.onclick=function(){var us=Object.keys(ssoValgt);if(!us.length)return;if(!confirm("Luk mitHF-adgang for de "+us.length+" valgte borgere?"))return;ssoSaet({us:us,til:"lukket"});};
    var rv=document.getElementById("ssoRydValg");if(rv)rv.onclick=function(){ssoValgt={};ssoTegn();};
}
async function ssoSaet(params){
    var sess=fbaSess();if(!sess)return;
    try{var r=await apiKald(Object.assign({a:"ssoadgangsaet",token:sess.token},params));
        if(r&&r.ok){
            ssoValgt={};ssoData=null;await renderSSOAdgang();
            var msg="✔ Opdateret — "+r.aendret+" ændret"+(r.sprunget>0?", "+r.sprunget+" sprunget over (mangler brugere-id)":"")+".";
            try{var t=document.createElement("div");t.textContent=msg;t.style.cssText="position:fixed;left:50%;bottom:26px;transform:translateX(-50%);z-index:99998;background:#1e374c;color:#fff;border-radius:12px;padding:11px 18px;font-family:var(--font);font-size:.86rem;box-shadow:0 14px 40px rgba(30,55,76,.4)";document.body.appendChild(t);setTimeout(function(){t.remove();},3500);}catch(e){}
        } else alert((r&&r.fejl)||"Kunne ikke gemme");
    }catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
}
async function renderGrupper(){
    const boks=document.getElementById("admGrupperBoks");if(!boks)return;
    const sess=fbaSess();if(!sess){boks.innerHTML='<p style="color:var(--roed)">Kræver login.</p>';return;}
    boks.innerHTML='<p style="color:var(--graa)">⏳ Henter grupper…</p>';
    let j;try{j=await apiKald({a:"taggrupper",token:sess.token});}catch(e){j=null;}
    if(!j||!j.ok){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente grupper.</p>';return;}
    grpModel=j;grpVis();
}
function grpTabbar(aktiv){
    var t=[["liste","🏷️ Tag-grupper"],["ret","👥 Efter rettighedsgruppe"],["medarb","🧑 Efter medarbejder"]];
    return '<div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:14px;border-bottom:1px solid var(--kant);padding-bottom:10px">'+
        t.map(function(x){return '<button class="grp-tab" data-v="'+x[0]+'" style="border:0;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.84rem;'+(aktiv===x[0]?'background:var(--moerk);color:#fff':'background:#eef3f7;color:#2a3a48')+'">'+x[1]+'</button>';}).join("")+'</div>';
}
function grpTabWire(boks){boks.querySelectorAll(".grp-tab").forEach(function(b){b.onclick=function(){grpVisning=b.getAttribute("data-v");grpVis();};});}
function grpVis(){if(grpVisning==="ret")return grpEfterRet();if(grpVisning==="medarb")return grpEfterMedarb();return grpListe();}
function grpAdgangTekst(g){
    if(!g.adgang||g.adgang==="alle")return "alle må bruge den";
    if(Array.isArray(g.adgang)){if(!g.adgang.length)return "ingen (kun via personer)";return "kun: "+g.adgang.map(function(x){return GRP_NAVNE[x]||x;}).join(", ");}
    return "alle må bruge den";
}
function grpListe(){
    const boks=document.getElementById("admGrupperBoks");if(!boks||!grpModel)return;
    var g=grpModel.grupper||[];
    var h=grpTabbar("liste")+'<div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;margin-bottom:12px">'+
        '<b style="font-size:1.05rem">🏷️ Tag-grupper <span style="color:var(--graa);font-weight:400;font-size:.82rem">organisatoriske grupper — kontorer, teams m.m. (ingen rettigheder)</span></b>'+
        (grpModel.kanStyre?'<button id="grpNyKnap" style="border:0;border-radius:12px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.85rem;color:#0d2a12;background:var(--groen)">+ Ny gruppe</button>':'')+
        '</div>';
    if(!g.length){h+='<p style="color:var(--graa)">Der er ingen tag-grupper endnu.'+(grpModel.kanStyre?' Opret den første med “+ Ny gruppe”.':'')+'</p>';}
    else{
        h+='<div style="display:flex;flex-direction:column;gap:8px">';
        g.forEach(function(x){
            h+='<div class="fba-kort" style="display:flex;align-items:center;gap:10px;padding:11px 13px;border-left:5px solid '+fbaEsc(x.farve||"#3cb4e5")+'">'+
                '<div style="flex:1;min-width:0"><div style="font-weight:800">'+fbaEsc(x.navn)+' <span style="color:var(--graa);font-weight:600;font-size:.8rem">@'+fbaEsc(x.handle)+'</span></div>'+
                '<div style="color:var(--graa);font-size:.8rem">'+x.antal+' medlem'+(x.antal===1?'':'mer')+' · '+fbaEsc(grpAdgangTekst(x))+'</div></div>'+
                (grpModel.kanStyre?'<button data-red="'+fbaEsc(x.gid)+'" style="border:1px solid var(--kant);background:#fff;border-radius:9px;padding:7px 12px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem">Redigér</button>'+
                    '<button data-slet="'+fbaEsc(x.gid)+'" data-navn="'+fbaEsc(x.navn)+'" style="border:1px solid #f2c9d4;background:#fff;color:var(--roed);border-radius:9px;padding:7px 12px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem">Slet</button>':'')+
                '</div>';
        });
        h+='</div>';
    }
    boks.innerHTML=h;grpTabWire(boks);
    var ny=document.getElementById("grpNyKnap");if(ny)ny.onclick=function(){grpEditor(null);};
    boks.querySelectorAll("button[data-red]").forEach(function(b){b.onclick=function(){grpEditor(b.getAttribute("data-red"));};});
    boks.querySelectorAll("button[data-slet]").forEach(function(b){b.onclick=function(){grpSlet(b.getAttribute("data-slet"),b.getAttribute("data-navn"));};});
}
function grpPersonHtml(idpre,valgte){
    var vis=grpModel.vis||{},admgrp=grpModel.admgrp||{},alle=grpModel.admins||[];
    var valgtSet={};(valgte||[]).forEach(function(l){valgtSet[l]=1;});
    var rows=alle.slice().sort(function(a,b){return (vis[a]||a).localeCompare(vis[b]||b,"da");}).map(function(l){
        var navn=vis[l]||l,grp=GRP_NAVNE[admgrp[l]]||admgrp[l]||"";
        return '<label data-navn="'+fbaEsc((navn+" "+grp).toLowerCase())+'" style="display:flex;align-items:center;gap:8px;padding:4px 6px;border-radius:7px;cursor:pointer;font-size:.86rem">'+
            '<input type="checkbox" data-l="'+fbaEsc(l)+'"'+(valgtSet[l]?' checked':'')+'> '+fbaEsc(navn)+
            (grp?' <span style="color:var(--graa);font-size:.76rem">'+fbaEsc(grp)+'</span>':'')+'</label>';
    }).join("");
    return '<input type="text" id="'+idpre+'Soeg" placeholder="Søg medarbejder…" style="width:100%;box-sizing:border-box;padding:7px 10px;border:1px solid var(--kant);border-radius:9px;font-family:var(--font);margin-bottom:6px">'+
        '<div style="display:flex;gap:6px;margin:0 0 6px">'+
        '<button type="button" id="'+idpre+'AlleTil" style="border:1px solid var(--kant);background:#fff;border-radius:8px;padding:5px 11px;cursor:pointer;font-family:var(--font);font-size:.78rem;font-weight:700;color:var(--moerk)">✓ Marker alle</button>'+
        '<button type="button" id="'+idpre+'AlleFra" style="border:1px solid var(--kant);background:#fff;border-radius:8px;padding:5px 11px;cursor:pointer;font-family:var(--font);font-size:.78rem;font-weight:700;color:var(--graa)">✕ Fravælg alle</button>'+
        '<span id="'+idpre+'Tael" style="margin-left:auto;align-self:center;color:var(--graa);font-size:.76rem"></span>'+
        '</div>'+
        '<div id="'+idpre+'Liste" style="max-height:180px;overflow:auto;border:1px solid var(--kant);border-radius:9px;padding:4px">'+rows+'</div>';
}
function grpPersonWire(idpre){
    var s=document.getElementById(idpre+"Soeg"),liste=document.getElementById(idpre+"Liste");
    if(!liste)return;
    var tael=document.getElementById(idpre+"Tael");
    function optael(){ if(tael){var n=liste.querySelectorAll("input[type=checkbox]:checked").length;tael.textContent=n?(n+" valgt"):"";} }
    if(s)s.oninput=function(){var q=s.value.toLowerCase().trim();liste.querySelectorAll("label").forEach(function(lb){lb.style.display=(!q||lb.getAttribute("data-navn").indexOf(q)>=0)?"flex":"none";});};
    /* Marker/Fravælg alle — virker på de SYNLIGE (respekterer søgefilteret) */
    function saet(v){liste.querySelectorAll("label").forEach(function(lb){if(lb.style.display!=="none"){var c=lb.querySelector('input[type=checkbox]');if(c)c.checked=v;}});optael();}
    var til=document.getElementById(idpre+"AlleTil"),fra=document.getElementById(idpre+"AlleFra");
    if(til)til.onclick=function(){saet(true);};
    if(fra)fra.onclick=function(){saet(false);};
    liste.addEventListener("change",optael);
    optael();
}

function grpPersonLaes(idpre){
    var liste=document.getElementById(idpre+"Liste");if(!liste)return [];
    return Array.prototype.map.call(liste.querySelectorAll("input[type=checkbox]:checked"),function(i){return i.getAttribute("data-l");});
}
function grpEditor(gid){
    const boks=document.getElementById("admGrupperBoks");if(!boks||!grpModel)return;
    var eks=null;if(gid)(grpModel.grupper||[]).forEach(function(x){if(x.gid===gid)eks=x;});
    var navn=eks?eks.navn:"",handle=eks?eks.handle:"",farve=(eks&&eks.farve)?eks.farve:"#3cb4e5";
    var medl=(eks&&eks.medlemmer)?eks.medlemmer:[],plus=(eks&&eks.plus)?eks.plus:[],minus=(eks&&eks.minus)?eks.minus:[];
    var adgAlle=!eks||!eks.adgang||eks.adgang==="alle";
    var adgValgt=(eks&&Array.isArray(eks.adgang))?eks.adgang:[];
    var retg=grpModel.retgrupper||[];
    var h='<div style="display:flex;align-items:center;gap:10px;margin-bottom:12px"><button id="grpTilbage" style="border:1px solid var(--kant);background:#fff;border-radius:9px;padding:7px 12px;cursor:pointer;font-family:var(--font);font-weight:700">← Tilbage</button>'+
        '<b style="font-size:1.05rem">'+(gid?"Redigér gruppe":"Ny tag-gruppe")+'</b></div>';
    h+='<div class="fba-kort" style="display:flex;flex-direction:column;gap:14px">';
    h+='<div style="display:flex;gap:12px;flex-wrap:wrap"><label style="flex:1;min-width:180px">Navn<br><input type="text" id="grpNavn" value="'+fbaEsc(navn)+'" placeholder="fx Kontor Odense" style="width:100%;box-sizing:border-box;padding:8px 10px;border:1px solid var(--kant);border-radius:9px;font-family:var(--font)"></label>'+
        '<label style="width:170px">@-handle<br><input type="text" id="grpHandle" value="'+fbaEsc(handle)+'" placeholder="auto fra navn" style="width:100%;box-sizing:border-box;padding:8px 10px;border:1px solid var(--kant);border-radius:9px;font-family:var(--font)"></label>'+
        '<label style="width:74px">Farve<br><input type="color" id="grpFarve" value="'+fbaEsc(farve)+'" style="width:100%;height:38px;border:1px solid var(--kant);border-radius:9px;background:#fff"></label></div>';
    h+='<div><b style="font-size:.9rem">Medlemmer</b><div style="color:var(--graa);font-size:.78rem;margin-bottom:6px">Hvem er i gruppen (bliver tagget / tilføjet kanaler)</div>'+grpPersonHtml("grpMedl",medl)+'</div>';
    h+='<div><b style="font-size:.9rem">Hvem må bruge gruppen?</b><div style="color:var(--graa);font-size:.78rem;margin-bottom:6px">Hvem kan tagge den / tilføje den til kanaler</div>'+
        '<label style="display:flex;gap:8px;align-items:center;margin-bottom:4px"><input type="radio" name="grpAdg" value="alle"'+(adgAlle?" checked":"")+'> Alle må bruge den</label>'+
        '<label style="display:flex;gap:8px;align-items:center;margin-bottom:6px"><input type="radio" name="grpAdg" value="valgt"'+(adgAlle?"":" checked")+'> Kun udvalgte rettighedsgrupper</label>'+
        '<div id="grpRetBoks" style="display:'+(adgAlle?"none":"flex")+';flex-wrap:wrap;gap:10px;padding:8px;border:1px solid var(--kant);border-radius:9px">'+
        retg.map(function(rg){return '<label style="display:flex;gap:6px;align-items:center;font-size:.85rem"><input type="checkbox" class="grpRg" value="'+fbaEsc(rg)+'"'+(adgValgt.indexOf(rg)>=0?" checked":"")+'> '+fbaEsc(GRP_NAVNE[rg]||rg)+'</label>';}).join("")+
        '</div></div>';
    h+='<details style="border:1px solid var(--kant);border-radius:9px;padding:8px"><summary style="cursor:pointer;font-weight:700;font-size:.86rem">Overstyring pr. person (avanceret)</summary>'+
        '<div style="margin-top:10px"><b style="font-size:.84rem;color:#1a7a1a">Giv ekstra personer adgang</b>'+grpPersonHtml("grpPlus",plus)+'</div>'+
        '<div style="margin-top:10px"><b style="font-size:.84rem;color:var(--roed)">Fjern adgang for personer</b>'+grpPersonHtml("grpMinus",minus)+'</div></details>';
    h+='<div style="display:flex;gap:10px"><button id="grpGem" style="border:0;border-radius:12px;padding:10px 18px;cursor:pointer;font-family:var(--font);font-weight:800;color:#0d2a12;background:var(--groen)">Gem gruppe</button>'+
        '<button id="grpAnnul" style="border:1px solid var(--kant);background:#fff;border-radius:12px;padding:10px 18px;cursor:pointer;font-family:var(--font);font-weight:700">Annullér</button>'+
        '<span id="grpStatus" style="align-self:center;color:var(--graa);font-size:.85rem"></span></div>';
    h+='</div>';
    boks.innerHTML=h;
    grpPersonWire("grpMedl");grpPersonWire("grpPlus");grpPersonWire("grpMinus");
    document.getElementById("grpTilbage").onclick=grpListe;
    document.getElementById("grpAnnul").onclick=grpListe;
    boks.querySelectorAll("input[name=grpAdg]").forEach(function(r){r.onchange=function(){document.getElementById("grpRetBoks").style.display=(document.querySelector("input[name=grpAdg]:checked").value==="valgt")?"flex":"none";};});
    document.getElementById("grpGem").onclick=function(){grpGem(gid);};
}
async function grpGem(gid){
    var st=document.getElementById("grpStatus");
    var navn=(document.getElementById("grpNavn").value||"").trim();
    if(!navn){alert("Gruppen skal have et navn");return;}
    var payload={a:"taggrupgem",token:fbaSess().token,navn:navn,
        handle:(document.getElementById("grpHandle").value||"").trim(),
        farve:document.getElementById("grpFarve").value||"#3cb4e5",
        medlemmer:grpPersonLaes("grpMedl"),plus:grpPersonLaes("grpPlus"),minus:grpPersonLaes("grpMinus")};
    if(gid)payload.gid=gid;
    if(document.querySelector("input[name=grpAdg]:checked").value==="valgt"){
        payload.adgang=Array.prototype.map.call(document.querySelectorAll(".grpRg:checked"),function(i){return i.value;});
    }
    if(st)st.textContent="Gemmer…";
    var r;try{r=await apiKald(payload);}catch(e){r=null;}
    if(r&&r.ok){await renderGrupper();}else{if(st)st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme gruppen");}
}
async function grpSlet(gid,navn){
    if(!confirm('Slet tag-gruppen “'+navn+'”? Den fjernes også fra kanaler, den er tilføjet.'))return;
    var r;try{r=await apiKald({a:"taggrupslet",token:fbaSess().token,gid:gid});}catch(e){r=null;}
    if(r&&r.ok){await renderGrupper();}else{alert((r&&r.fejl)||"Kunne ikke slette");}
}

/* ---- spejlvisning: EFTER RETTIGHEDSGRUPPE (standard-tilgængelighed) ---- */
function grpEfterRet(){
    var boks=document.getElementById("admGrupperBoks");if(!boks||!grpModel)return;
    var retg=grpModel.retgrupper||[];
    if(!grpErValg||retg.indexOf(grpErValg)<0)grpErValg=retg[0]||"";
    var grupper=grpModel.grupper||[];
    var h=grpTabbar("ret");
    h+='<div style="margin-bottom:12px"><b style="font-size:1.02rem">👥 Tilgængelige tag-grupper pr. rettighedsgruppe</b>'+
        '<div style="color:var(--graa);font-size:.82rem">Sæt hvilke tag-grupper medlemmer af en rettighedsgruppe som standard kan bruge. Overstyring pr. person laver du under “Efter medarbejder”.</div></div>';
    h+='<label style="font-weight:700;font-size:.9rem">Rettighedsgruppe: <select id="grpErSel" style="padding:7px 10px;border:1px solid var(--kant);border-radius:9px;font-family:var(--font)">'+
        retg.map(function(r){return '<option value="'+fbaEsc(r)+'"'+(r===grpErValg?" selected":"")+'>'+fbaEsc(GRP_NAVNE[r]||r)+'</option>';}).join("")+'</select></label>';
    if(!grupper.length){h+='<p style="color:var(--graa);margin-top:12px">Der er ingen tag-grupper endnu.</p>';}
    else{
        h+='<div style="display:flex;flex-direction:column;gap:6px;margin-top:12px">';
        grupper.forEach(function(g){
            var alle=(!g.adgang||g.adgang==="alle");
            var tilgg=alle||(Array.isArray(g.adgang)&&g.adgang.indexOf(grpErValg)>=0);
            h+='<label style="display:flex;align-items:center;gap:10px;padding:8px 11px;border:1px solid var(--kant);border-radius:9px;cursor:pointer">'+
                '<input type="checkbox" class="grp-er-cb" data-g="'+fbaEsc(g.gid)+'"'+(tilgg?" checked":"")+'>'+
                '<span style="width:12px;height:12px;border-radius:3px;background:'+fbaEsc(g.farve||"#3cb4e5")+';display:inline-block"></span>'+
                '<span style="font-weight:700">'+fbaEsc(g.navn)+'</span>'+
                (alle?'<span style="color:var(--graa);font-size:.76rem">(åben for alle grupper)</span>':'')+'</label>';
        });
        h+='</div><div id="grpErStatus" style="margin-top:8px;color:var(--graa);font-size:.82rem"></div>';
    }
    boks.innerHTML=h;grpTabWire(boks);
    var sel=document.getElementById("grpErSel");if(sel)sel.onchange=function(){grpErValg=sel.value;grpEfterRet();};
    boks.querySelectorAll(".grp-er-cb").forEach(function(cb){cb.onchange=function(){grpErToggle(cb.getAttribute("data-g"),cb.checked);};});
}
async function grpErToggle(gid,tilfoej){
    var g=(grpModel.grupper||[]).filter(function(x){return x.gid===gid;})[0];if(!g)return;
    var retg=grpModel.retgrupper||[];
    var liste;
    if(!g.adgang||g.adgang==="alle")liste=retg.slice();
    else if(Array.isArray(g.adgang))liste=g.adgang.slice();
    else liste=[];
    var ix=liste.indexOf(grpErValg);
    if(tilfoej&&ix<0)liste.push(grpErValg);
    if(!tilfoej&&ix>=0)liste.splice(ix,1);
    var erAlle=retg.length&&retg.every(function(r){return liste.indexOf(r)>=0;});
    var st=document.getElementById("grpErStatus");if(st)st.textContent="Gemmer…";
    var r;try{r=await apiKald({a:"taggrupadgang",token:fbaSess().token,gid:gid,adgang:erAlle?"alle":liste});}catch(e){r=null;}
    if(r&&r.ok){g.adgang=r.adgang;if(st)st.textContent="✔ Gemt";}
    else{if(st)st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");grpEfterRet();}
}
/* ---- spejlvisning: EFTER MEDARBEJDER (overstyring pr. person) ---- */
function grpEfterMedarb(){
    var boks=document.getElementById("admGrupperBoks");if(!boks||!grpModel)return;
    var admins=grpModel.admins||[],vis=grpModel.vis||{},admgrp=grpModel.admgrp||{};
    var sorted=admins.slice().sort(function(a,b){return (vis[a]||a).localeCompare(vis[b]||b,"da");});
    if(!grpMedValg||admins.indexOf(grpMedValg)<0)grpMedValg=sorted[0]||"";
    var minGrp=admgrp[grpMedValg]||"leder";
    var grupper=grpModel.grupper||[];
    var h=grpTabbar("medarb");
    h+='<div style="margin-bottom:12px"><b style="font-size:1.02rem">🧑 Adgang pr. medarbejder</b>'+
        '<div style="color:var(--graa);font-size:.82rem">Vælg en medarbejder og overstyr adgangen til de enkelte tag-grupper. “Standard” følger vedkommendes rettighedsgruppe.</div></div>';
    h+='<label style="font-weight:700;font-size:.9rem">Medarbejder: <select id="grpMedSel" style="padding:7px 10px;border:1px solid var(--kant);border-radius:9px;font-family:var(--font)">'+
        sorted.map(function(l){return '<option value="'+fbaEsc(l)+'"'+(l===grpMedValg?" selected":"")+'>'+fbaEsc(vis[l]||l)+' ('+fbaEsc(GRP_NAVNE[admgrp[l]]||admgrp[l]||"")+')</option>';}).join("")+'</select></label>';
    if(!grupper.length){h+='<p style="color:var(--graa);margin-top:12px">Ingen tag-grupper endnu.</p>';}
    else{
        h+='<div style="display:flex;flex-direction:column;gap:6px;margin-top:12px">';
        grupper.forEach(function(g){
            var plus=(g.plus||[]).indexOf(grpMedValg)>=0,minus=(g.minus||[]).indexOf(grpMedValg)>=0;
            var grpAdg=(!g.adgang||g.adgang==="alle")?true:(Array.isArray(g.adgang)?g.adgang.indexOf(minGrp)>=0:true);
            var eff=minus?false:(plus?true:grpAdg),valg=plus?"plus":(minus?"minus":"std");
            h+='<div style="display:flex;align-items:center;gap:10px;padding:8px 11px;border:1px solid var(--kant);border-radius:9px">'+
                '<span style="width:12px;height:12px;border-radius:3px;background:'+fbaEsc(g.farve||"#3cb4e5")+';display:inline-block"></span>'+
                '<span style="flex:1;font-weight:700">'+fbaEsc(g.navn)+' <span style="font-weight:400;color:'+(eff?"#1a7a1a":"var(--roed)")+';font-size:.78rem">'+(eff?"✓ har adgang":"✗ ingen adgang")+'</span></span>'+
                '<select class="grp-med-sel" data-g="'+fbaEsc(g.gid)+'" style="padding:5px 8px;border:1px solid var(--kant);border-radius:8px;font-family:var(--font);font-size:.82rem">'+
                '<option value="std"'+(valg==="std"?" selected":"")+'>Standard ('+(grpAdg?"adgang":"ingen")+')</option>'+
                '<option value="plus"'+(valg==="plus"?" selected":"")+'>Altid adgang</option>'+
                '<option value="minus"'+(valg==="minus"?" selected":"")+'>Aldrig adgang</option>'+
                '</select></div>';
        });
        h+='</div><div id="grpMedStatus" style="margin-top:8px;color:var(--graa);font-size:.82rem"></div>';
    }
    boks.innerHTML=h;grpTabWire(boks);
    var sel=document.getElementById("grpMedSel");if(sel)sel.onchange=function(){grpMedValg=sel.value;grpEfterMedarb();};
    boks.querySelectorAll(".grp-med-sel").forEach(function(s){s.onchange=function(){grpMedSet(s.getAttribute("data-g"),s.value);};});
}
async function grpMedSet(gid,valg){
    var g=(grpModel.grupper||[]).filter(function(x){return x.gid===gid;})[0];if(!g)return;
    var plus=(g.plus||[]).slice().filter(function(l){return l!==grpMedValg;});
    var minus=(g.minus||[]).slice().filter(function(l){return l!==grpMedValg;});
    if(valg==="plus")plus.push(grpMedValg);
    if(valg==="minus")minus.push(grpMedValg);
    var st=document.getElementById("grpMedStatus");if(st)st.textContent="Gemmer…";
    var r;try{r=await apiKald({a:"taggrupadgang",token:fbaSess().token,gid:gid,plus:plus,minus:minus});}catch(e){r=null;}
    if(r&&r.ok){g.plus=r.plus;g.minus=r.minus;grpEfterMedarb();}
    else{if(st)st.textContent="";alert((r&&r.fejl)||"Kunne ikke gemme");}
}

/* ===================== ⚙️ Brugere & rettigheder (kun ejeren) ===================== */
const RET_NAVNE={chat:"Live-chat",feedback:"Feedback",intern:"Intern komm.",redigerbesked:"Redigér egne beskeder",nyheder:"Nyheder",
    aarshjul_alle:"Årshjul (fælles)",aarshjul_borger:"Årshjul (borger)",konsulent:"Skift konsulent",stat:"Statistik",moderation:"Moderation",
    admopret:"Styr admin-adgang",borgeropret:"Opret borgere",programmer:"Redigér programlisten",sektioner:"Tilpas sektionerne",kanaler:"Opret/administrér kanaler",statusandre:"Sæt status for kolleger",assistent:"HF-assistent",tekster:"Redigér tekster",support:"Behandle support",support_tid:"Ændre support-tider",taggrupper:"Styr tag-grupper",dashboard:"Dashboard",sso:"mitHF-adgang",borgerslet:"Slet ophørte borgere",guider:"Redigér guides",salg:"HF Salg (CRM)"};
/* Kort forklaring pr. rettighed — vises i en lille boks når man trykker på ⓘ */
const RET_HJAELP={
    chat:"Kan besvare borgernes live-chats (kundeservice-chatten i universet).",
    feedback:"Kan se og håndtere den feedback, borgerne sender ind.",
    intern:"Adgang til intern kommunikation — kanaler og private tråde mellem medarbejdere.",
    redigerbesked:"Kan redigere sine EGNE opslag i kanaler og private tråde (rettede opslag markeres «redigeret»). Redigering af ANDRES opslag kræver desuden Moderation.",
    nyheder:"Kan oprette og redigere nyheder, som borgerne ser i universet.",
    aarshjul_alle:"Kan oprette og redigere FÆLLES årshjuls-datoer, der gælder alle borgere.",
    aarshjul_borger:"Kan oprette og redigere årshjuls-datoer for ENKELTE borgere.",
    konsulent:"Kan ændre hvilken konsulent en borger er tilknyttet.",
    stat:"Adgang til det ældre statistik-område.",
    moderation:"Kan moderere borgernes fællesskabs-opslag — godkende eller fjerne indhold.",
    admopret:"Kan styre medarbejdernes admin-adgang og rettigheder (dette område). Giv kun til ledelsen.",
    borgeropret:"Kan oprette nye borgere og åbne/lukke borgeres adgang til universet.",
    programmer:"Kan redigere den fælles programliste (genveje til systemer).",
    sektioner:"Kan tilpasse forsidens sektioner og rækkefølge for alle.",
    kanaler:"Kan oprette og administrere kanaler i den interne kommunikation.",
    statusandre:"Kan sætte til stede-status for andre medarbejdere (ikke kun sig selv).",
    assistent:"Kan bruge HF-assistenten — den interne AI-hjælper til medarbejdere.",
    tekster:"Kan redigere borger-teksterne: FAQ, forsidetekster, kontakt m.m.",
    support:"Kan behandle borgernes support- og beskedsager.",
    support_tid:"Kan ændre support-åbningstider og de automatiske svartider.",
    taggrupper:"Kan oprette og styre tag-grupper (organisatoriske grupper på tværs af rettigheder).",
    dashboard:"Adgang til Dashboardet — hvem er online lige nu + aktivitets- og forbrugsstatistik.",
    sso:"Kan styre hvilke borgere der kan logge direkte ind i mitHF fra universet (aktivér/luk adgang).",
    borgerslet:"Kan se listen over OPHØRTE borgere (med lukket adgang) og slette dem permanent. ALT deres data fjernes uigenkaldeligt. Giv kun til ledelsen.",
    guider:"Kan oprette og redigere guides i 📖 Guider-området. Alle medarbejdere kan læse guiderne — dette styrer kun, hvem der må skrive/rette dem.",
    salg:"Adgang til 🧩 Apps → 💰 HF Salg (salgssystem/CRM). Kun brugere med denne rettighed (og ejeren) kan se og åbne salgsmodulet."
};
function visRetHjaelp(el,pm){
    var gl=document.querySelectorAll(".ret-hjaelp-pop"); for(var i=0;i<gl.length;i++)gl[i].remove();
    if(el.getAttribute("data-aaben")==="1"){el.removeAttribute("data-aaben");return;}
    var alle=document.querySelectorAll(".ret-hjaelp"); for(var j=0;j<alle.length;j++)alle[j].removeAttribute("data-aaben");
    var pop=document.createElement("div"); pop.className="ret-hjaelp-pop";
    pop.style.cssText="position:absolute;z-index:9999;max-width:260px;background:#1e374c;color:#fff;padding:10px 13px;border-radius:10px;font-size:.8rem;line-height:1.45;box-shadow:0 8px 24px rgba(0,0,0,.25)";
    var t=document.createElement("div"); t.style.cssText="font-weight:800;margin-bottom:3px"; t.textContent=(RET_NAVNE[pm]||pm);
    var d=document.createElement("div"); d.textContent=(RET_HJAELP[pm]||"Ingen beskrivelse endnu.");
    pop.appendChild(t); pop.appendChild(d); document.body.appendChild(pop);
    var r=el.getBoundingClientRect(), top=r.bottom+window.scrollY+6, left=r.left+window.scrollX-8;
    var bw=pop.offsetWidth, maxL=window.scrollX+document.documentElement.clientWidth-bw-8;
    if(left>maxL)left=maxL; if(left<8)left=8;
    pop.style.top=top+"px"; pop.style.left=left+"px"; el.setAttribute("data-aaben","1");
    setTimeout(function(){
        var luk=function(ev){ if(!pop.contains(ev.target)&&ev.target!==el){ pop.remove(); el.removeAttribute("data-aaben"); document.removeEventListener("click",luk); } };
        document.addEventListener("click",luk);
    },0);
}
const GRP_NAVNE={direktion:"Direktion",leder:"Leder",konsulent:"Konsulent",hr:"HR",loen:"Løn",booker:"Booker"};
/* Medarbejdere m. titel, mail og gruppe (vurderet ud fra titel) — bruges af 🚀 masse-aktivering.
   Søren Rynkebjerg er bevidst udeladt (han er ejeren via sorenadm). */
const MEDARB_DATA=[
    ["Anders Christiansen","DPO","ac@handicapformidlingen.dk","hr"],
    ["Anika Lyhne Milde Andersen","Lønmedarbejder","anika@handicapformidlingen.dk","loen"],
    ["Anita Monberg Mouritsen","Konsulent","anita@handicapformidlingen.dk","konsulent"],
    ["Anne Larsen","HR medarbejder","anne@handicapformidlingen.dk","hr"],
    ["Brianne Holm Simon","Teamleder","brianne@handicapformidlingen.dk","leder"],
    ["Ceci Mott Christiansen","BPA konsulent","ceci@handicapformidlingen.dk","konsulent"],
    ["Charlotta Nysted","Konsulent","charlotta@handicapformidlingen.dk","konsulent"],
    ["Charlotte Aarfelt","Booker","ca@handicapformidlingen.dk","booker"],
    ["Christian Sonne","Økonomimedarbejder","christian@handicapformidlingen.dk","loen"],
    ["Christina Brasgaard","Konsulent","cb@handicapformidlingen.dk","konsulent"],
    ["Christina Klarskov Christensen","Teamleder","ckc@handicapformidlingen.dk","leder"],
    ["Christina Marchi-Uhlén","Økonomi","christina@handicapformidlingen.dk","loen"],
    ["David Bøtker","Chefkonsulent","david@handicapformidlingen.dk","leder"],
    ["Dea Andersen","BPA konsulent","dea@handicapformidlingen.dk","konsulent"],
    ["Elisabeth Saandvig","HR","elisabeth@handicapformidlingen.dk","hr"],
    ["Emma Krejberg","Kvalitetsansvarlig","emma@handicapformidlingen.dk","konsulent"],
    ["Frederik Toft Larsen","Salgschef","frederik@handicapformidlingen.dk","leder"],
    ["Henriette Aaen Friis","Booker","henriette@handicapformidlingen.dk","booker"],
    ["Ida Josefine Heiselberg Sørensen","HR","ida@handicapformidlingen.dk","hr"],
    ["Janni Lennø","Konsulent","jl@handicapformidlingen.dk","konsulent"],
    ["Jeanette Emde","Økonomi","jeanette@handicapformidlingen.dk","loen"],
    ["Jesper Ebbesen","Konsulent","jesper@handicapformidlingen.dk","konsulent"],
    ["Karen Karadottir","HR medarbejder","karen@handicapformidlingen.dk","hr"],
    ["Katja Holm","Driftchef øst","kh@handicapformidlingen.dk","leder"],
    ["Kia Kildegaard","Konsulent","kia@handicapformidlingen.dk","konsulent"],
    ["Kim Haar","IT ansvarlig","kim@handicapformidlingen.dk","leder"],
    ["Klaus Dübe","Udviklingsdirektør","klaus@handicapformidlingen.dk","direktion"],
    ["Kristine Møller Korsholm","HR","kristine@handicapformidlingen.dk","hr"],
    ["Laila Petersen","Sundhedsfaglig teamleder","laila@handicapformidlingen.dk","leder"],
    ["Laura Stenager Munk Nielsen","Kvalitetsansvarlig","laura@handicapformidlingen.dk","konsulent"],
    ["Lene Skytte Hansen","Booker","lene@handicapformidlingen.dk","booker"],
    ["Liza Christensen","Konsulent","liza@handicapformidlingen.dk","konsulent"],
    ["Louise Lindersson Olsen","Lønmedarbejder","llo@handicapformidlingen.dk","loen"],
    ["Magnus Fristrup","Personalechef","magnus@handicapformidlingen.dk","leder"],
    ["Mai-Britt Holt Moesgaard","Akutteam koordinator","mhm@handicapformidlingen.dk","leder"],
    ["Maja Dahl","Booker","md@handicapformidlingen.dk","booker"],
    ["Maria Andersen","Konsulent","ma@handicapformidlingen.dk","konsulent"],
    ["Maria Hørby","Lønmedarbejder","mh@handicapformidlingen.dk","loen"],
    ["Maria Jensen","HR","mj@handicapformidlingen.dk","hr"],
    ["Maria Kronborg","Sundhedsfaglig teamleder","mk@handicapformidlingen.dk","leder"],
    ["Marianne Larsen","Konsulent","marianne@handicapformidlingen.dk","konsulent"],
    ["Mariia Andersen","Konsulent","mca@handicapformidlingen.dk","konsulent"],
    ["Mathias Ekknud","Salgskonsulent","me@handicapformidlingen.dk","konsulent"],
    ["Mathias Rechnagel","Økonomi","mathias@handicapformidlingen.dk","loen"],
    ["Mette Bryld","Konsulent","mb@handicapformidlingen.dk","konsulent"],
    ["Mette Bysøe","Sundhedsfaglig teamleder","metteb@handicapformidlingen.dk","leder"],
    ["Mia Bloch Andersen","Sundhedsfaglig teamleder","mia@handicapformidlingen.dk","leder"],
    ["Mia Rasmussen","S/A","mr@handicapformidlingen.dk","konsulent"],
    ["Michael Mark","Konsulent","michael@handicapformidlingen.dk","konsulent"],
    ["Michelle Jensen","HR medarbejder","michelle@handicapformidlingen.dk","hr"],
    ["Nanna Christensen","Booker","nanna@handicapformidlingen.dk","booker"],
    ["Per Dübe","Chefkonsulent","per@handicapformidlingen.dk","leder"],
    ["Peter Helby","Juridisk direktør","peter@handicapformidlingen.dk","direktion"],
    ["Randi Elholm","Økonomimedarbejder","randi@handicapformidlingen.dk","loen"],
    ["Rasmus Just","Konsulent","just@handicapformidlingen.dk","konsulent"],
    ["Rasmus Snedker Sørensen","Chefkonsulent","rasmus@handicapformidlingen.dk","leder"],
    ["Regin Christensen","Konsulent","regin@handicapformidlingen.dk","konsulent"],
    ["Rikke Stoklund","Konsulent","rikke@handicapformidlingen.dk","konsulent"],
    ["Sabina Carlsen","HR konsulent","sabina@handicapformidlingen.dk","hr"],
    ["Sanne Brenfelt","Lønmedarbejder","sanne@handicapformidlingen.dk","loen"],
    ["Sarah Struwe Harritsø","Administrativ medarbejder","sarah@handicapformidlingen.dk","konsulent"],
    ["Shagha Hansen","Booker","shagha@handicapformidlingen.dk","booker"],
    ["Sigrid Johanna Rossen","Specialkonsulent","sigrid@handicapformidlingen.dk","konsulent"],
    ["Simon Bastholm","Konsulent","sb@handicapformidlingen.dk","konsulent"],
    ["Sisse Frankfurth","Konsulent","sisse@handicapformidlingen.dk","konsulent"],
    ["Steffan Poulsen","HR konsulent","steffan@handicapformidlingen.dk","hr"],
    ["Stephanie Frydensbjerg Jensen","HR medarbejder","stephanie@handicapformidlingen.dk","hr"],
    ["Søren Barslund Liboriussen","S/A","sl@handicapformidlingen.dk","konsulent"],
    ["Therese Jensen","Konsulent","therese@handicapformidlingen.dk","konsulent"],
    ["Thomas Blond","Konsulent","tbn@handicapformidlingen.dk","konsulent"],
    ["Thomas Søe","Konsulent","thomas@handicapformidlingen.dk","konsulent"],
    ["Tobias Hørby Broager","Konsulent","thb@handicapformidlingen.dk","konsulent"],
    ["Trine Drangsfeldt","S/A","trine@handicapformidlingen.dk","konsulent"],
    ["Trine Støvring Kristensen","Booker","tsk@handicapformidlingen.dk","booker"],
    ["Vibeke Thomsen","Konsulent","vibeke@handicapformidlingen.dk","konsulent"],
    ["Vicky Dose","PA/EA","vic@handicapformidlingen.dk","konsulent"],
    ["Vivi Mathiasen","Respiratorisk ansvarlig syd/vest","vivi@handicapformidlingen.dk","konsulent"]];
let retData=null;
/* 🚫 Ophørte borgere — liste + permanent sletning (kræver server-perm 'borgerslet') */
async function renderOphoerte(){
    const boks=document.getElementById("ophoertBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    let j=null;try{j=await apiKald({a:"ophoertliste",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){boks.innerHTML='<p style="color:var(--graa);font-size:.85rem">Kunne ikke hente listen (kræver «Slet ophørte borgere»-rettigheden).</p>';return;}
    const b=j.borgere||[];
    if(!b.length){boks.innerHTML='<p style="color:var(--graa);font-size:.85rem">Ingen ophørte borgere lige nu. 🎉</p>';return;}
    boks.innerHTML='<div style="display:grid;gap:6px">'+b.map(x=>{
        let dato="";try{if(x.lukket)dato=new Date(x.lukket*1000).toLocaleDateString("da-DK");}catch(e){}
        return '<div class="fba-top" style="padding:8px 12px;background:var(--flade);border-radius:10px;border:1px solid rgba(30,55,76,.07)">'+
            '<span class="fba-hvem">'+fbaEsc(x.navn||"(uden navn)")+' <span style="color:var(--graa);font-weight:400">('+fbaEsc(x.u)+')</span></span>'+
            (dato?'<span style="color:var(--graa);font-size:.75rem;margin-left:8px">lukket '+fbaEsc(dato)+'</span>':'')+
            '<button class="fba-done ophoert-genaaben" data-u="'+fbaEsc(x.u)+'" data-n="'+fbaEsc(x.navn||"")+'" title="Fortryd hvis adgangen blev lukket ved en fejl" style="margin-left:auto;border:0;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem;background:rgba(147,213,0,.2);color:#41630a">✅ Genåbn adgang</button>'+
            '<button class="fba-done ophoert-slet" data-u="'+fbaEsc(x.u)+'" data-n="'+fbaEsc(x.navn||"")+'" style="margin-left:6px;border:0;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem;background:rgba(231,0,76,.12);color:var(--roed)">🗑 Slet permanent</button>'+
            '</div>';
    }).join("")+'</div>';
    boks.querySelectorAll(".ophoert-genaaben").forEach(btn=>{
        btn.onclick=async function(){
            const u=this.getAttribute("data-u"),navn=this.getAttribute("data-n")||u;
            if(!confirm("Genåbn adgangen for "+navn+" ("+u+")?\n\nBorgeren kan igen logge ind i universet og forsvinder fra listen over ophørte."))return;
            this.disabled=true;this.textContent="⏳ Genåbner …";
            try{const r=await apiKald({a:"borgeradgangtoggle",token:sess.token,login:u,til:1,navn:navn});
                if(r&&r.ok){renderOphoerte();}
                else{this.disabled=false;this.textContent="✅ Genåbn adgang";alert((r&&r.fejl)||"Kunne ikke genåbne");}}
            catch(e){this.disabled=false;this.textContent="✅ Genåbn adgang";alert("Kunne ikke genåbne — tjek forbindelsen.");}
        };
    });
    boks.querySelectorAll(".ophoert-slet").forEach(btn=>{
        btn.onclick=async function(){
            const u=this.getAttribute("data-u"),navn=this.getAttribute("data-n")||u;
            if(!confirm("Slet "+navn+" ("+u+") PERMANENT?\n\nALT borgerens data fjernes uigenkaldeligt:\n• stamdata, login og adgang\n• beskeder, support og feedback\n• noter, kalender, fællesskab og chats\n\nDette kan IKKE fortrydes."))return;
            const svar=(window.prompt('Er du HELT sikker? Skriv borgerens kundenr «'+u+'» for at bekræfte den permanente sletning:')||"").trim();
            if(svar!==u){if(svar)alert("Kundenr matchede ikke — sletningen er annulleret.");return;}
            this.disabled=true;this.textContent="⏳ Sletter …";
            try{const r=await apiKald({a:"borgerslet",token:sess.token,u:u});
                if(r&&r.ok){renderOphoerte();}
                else{this.disabled=false;this.textContent="🗑 Slet permanent";alert((r&&r.fejl)||"Kunne ikke slette");}}
            catch(e){this.disabled=false;this.textContent="🗑 Slet permanent";alert("Kunne ikke slette — tjek forbindelsen.");}
        };
    });
}
async function renderRettigheder(){
    const boks=document.getElementById("admRetBoks"),sess=fbaSess();
    if(!boks||!sess)return;
    if(!retData){
        try{const j=await apiKald({a:"admprofiler",token:sess.token});if(j&&j.ok)retData=j;}catch(e){}
        if(!retData){boks.innerHTML='<p style="color:var(--roed)">Kunne ikke hente.</p>';return;}
    }
    const perms=retData.perms||[],grupper=retData.grupper||{},logins=retData.logins||[];
    /* ---- 1) GRUPPE-RETTIGHEDER: standarderne, som gælder alle i gruppen ---- */
    let html='<details class="fba-kort" style="background:#fff"><summary style="cursor:pointer;font-weight:800">🧩 Gruppe-rettigheder <span style="color:var(--graa);font-weight:400;font-size:.8rem">standard for alle i gruppen — undtagelser sættes pr. person nedenfor</span></summary>'+
        Object.keys(GRP_NAVNE).map(g=>{
            const std=grupper[g]||{};
            return '<details style="margin:8px 0;background:var(--flade);border-radius:11px;padding:8px 13px">'+
                '<summary style="cursor:pointer;font-weight:700">'+GRP_NAVNE[g]+'</summary>'+
                '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(185px,1fr));gap:6px;margin-top:8px;font-size:.85rem">'+
                perms.map(pm=>'<span style="display:flex;gap:6px;align-items:center"><label style="display:flex;gap:7px;align-items:center;flex:1;min-width:0"><input type="checkbox" class="grp-ret" data-g="'+g+'" data-p="'+pm+'" '+(std[pm]?"checked":"")+'> '+RET_NAVNE[pm]+'</label><span class="ret-hjaelp" data-hp="'+pm+'" title="Forklaring" style="cursor:pointer;color:#3cb4e5;font-size:.8rem;font-weight:700;user-select:none">ⓘ</span></span>').join("")+'</div>'+
                '<button class="fba-send grp-gem" data-g="'+g+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;margin-top:8px">💾 Gem '+GRP_NAVNE[g]+'</button>'+
                '</details>';
        }).join("")+'</details>';

    /* ---- 2) BRUGERE: alle interne + admins — adgang togles, rolle styres ---- */
    const prLogin={};
    Object.keys(retData.profiler).forEach(uid=>{prLogin[retData.profiler[uid].login]=uid;});
    const interne=(typeof adgangsBorgere==="function"?adgangsBorgere(true):[]).filter(b=>!/^20\d{4}$/.test(b.u));
    const udenLogin=interne.filter(b=>!prLogin[b.u]&&logins.indexOf(b.u)<0);
    /* søgefelt + gruppér brugerne efter deres rettighedsgruppe (kompakt, foldbart) */
    const RET_GRP_ORDEN=["direktion","leder","konsulent","hr","loen","booker"];
    const retBadge='display:inline-block;font-size:.7rem;font-weight:700;border-radius:999px;padding:1px 8px';
    function retBrugerHTML(uid){
        const p=retData.profiler[uid];
        const std=grupper[p.gruppe]||{};
        const lukket=!!p.deaktiveret;
        const harLogin=logins.indexOf(p.login)>=0;
        const sog=((p.vis||"")+" "+(p.login||"")+" "+(p.titel||"")+" "+(GRP_NAVNE[p.gruppe]||p.gruppe||"")).toLowerCase();
        return '<details class="ret-bruger" data-sog="'+fbaEsc(sog)+'" style="margin:5px 0;background:#fff;border-radius:10px;border:1px solid '+(lukket?'rgba(231,0,76,.3)':'rgba(30,55,76,.08)')+';padding:6px 12px'+(lukket||!harLogin?';opacity:.7':'')+'">'+
            '<summary style="cursor:pointer;font-weight:700;font-size:.9rem;display:flex;align-items:center;gap:8px;flex-wrap:wrap">'+
            '<span>'+fbaEsc(p.vis||p.login)+'</span>'+
            '<span style="color:var(--graa);font-weight:400;font-size:.78rem">'+fbaEsc(p.login)+'</span>'+
            (p.ejer?'<span style="'+retBadge+';background:rgba(245,166,35,.18);color:#a86b00">👑 Ejer</span>':'')+
            (lukket?'<span style="'+retBadge+';background:rgba(231,0,76,.12);color:var(--roed)">⛔ lukket</span>':'')+
            (harLogin?'':'<span style="'+retBadge+';background:rgba(245,166,35,.18);color:#a86b00">⚠️ intet kodeord</span>')+
            '</summary>'+
            (harLogin||p.ejer?'':'<p style="color:var(--graa);font-size:.8rem;margin:6px 0 0">Kodeordet er fjernet (fx fra admin-brugere.php). Brugeren kan ikke logge ind. Sæt et nyt med 🔑 — eller fjern profilen, hvis den ikke skal bruges (historikken beholder navnet).</p>')+
            '<div style="display:grid;gap:8px;margin-top:10px;font-size:.86rem">'+
            '<label>Visningsnavn (det borgerne ser): <input class="ret-vis" data-uid="'+uid+'" value="'+fbaEsc(p.vis||"")+'" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font)"></label>'+
            '<div style="display:flex;gap:10px;flex-wrap:wrap">'+
            '<label>Titel: <input class="ret-titel" data-uid="'+uid+'" value="'+fbaEsc(p.titel||"")+'" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:220px"></label>'+
            '<label>Mail: <input class="ret-mail" data-uid="'+uid+'" value="'+fbaEsc(p.mail||"")+'" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:260px"></label>'+
            '<label title="Vises automatisk på dagen — Min dag + officiel kanal">🎂 Fødselsdag (dd-mm): <input class="ret-fd" data-uid="'+uid+'" value="'+fbaEsc(p.foedselsdag||"")+'" placeholder="24-06" maxlength="5" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:80px"></label>'+
            '<label title="Bruges til jubilæums-hilsner">🏅 Ansat (dd-mm-åååå): <input class="ret-ansat" data-uid="'+uid+'" value="'+fbaEsc(p.ansat||"")+'" placeholder="01-08-2019" maxlength="10" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:110px"></label>'+
            '<label title="Til direkte profil-link: …page=admin_brugere_show&admin_brugere_id=<id>">🩵 mitHF-id: <input class="ret-mid" data-uid="'+uid+'" value="'+fbaEsc(p.mithfadmid||"")+'" placeholder="190" maxlength="8" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:70px">'+
            (p.mithfadmid?' <a href="https://handicapformidlingen.temponizer.dk/index.php?page=admin_brugere_show&admin_brugere_id='+fbaEsc(p.mithfadmid)+'" target="_blank" rel="noopener" title="Åbn profilen i mitHF" style="font-size:.8rem">↗</a>':'')+'</label>'+
            '<label>📞 Tlf: <input class="ret-tlf" data-uid="'+uid+'" value="'+fbaEsc(p.tlf||"")+'" maxlength="20" placeholder="12 34 56 78" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:6px 10px;font-family:var(--font);width:110px"></label>'+
            '<label title="Vises på personens profilkort (360°)">☎️ Direkte nr <span style="color:var(--graa);font-weight:400">(på kortet)</span>: <input class="ret-direkte" data-uid="'+uid+'" value="'+fbaEsc(p.direkte||"")+'" maxlength="20" placeholder="71 99 73 78" style="border:1px solid rgba(60,180,229,.45);border-radius:8px;padding:6px 10px;font-family:var(--font);width:120px"></label></div>'+
            (p.ejer?'<span style="color:var(--graa)">👑 Ejeren har altid alle rettigheder — også hvis brugernavnet ændres.</span>'
                :'<label>Gruppe: <select class="ret-grp" data-uid="'+uid+'" style="font-family:var(--font);padding:6px 10px;border-radius:8px;border:1px solid rgba(30,55,76,.2)">'+
                Object.keys(GRP_NAVNE).map(g=>'<option value="'+g+'" '+(g===p.gruppe?"selected":"")+'>'+GRP_NAVNE[g]+'</option>').join("")+'</select></label>'+
                '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(190px,1fr));gap:6px">'+
                perms.map(pm=>{
                    const o=(p.over&&p.over[pm]!==undefined)?String(p.over[pm]):"";
                    return '<div style="display:flex;gap:6px;align-items:center"><span>'+RET_NAVNE[pm]+'</span><span class="ret-hjaelp" data-hp="'+pm+'" title="Forklaring" style="cursor:pointer;color:#3cb4e5;font-size:.78rem;font-weight:700;user-select:none">ⓘ</span>'+
                        '<select class="ret-ov" data-uid="'+uid+'" data-p="'+pm+'" style="font-family:var(--font);padding:3px 6px;border-radius:7px;border:1px solid rgba(30,55,76,.2);font-size:.8rem">'+
                        '<option value="" '+(o===""?"selected":"")+'>Gruppe ('+(std[pm]?"ja":"nej")+')</option>'+
                        '<option value="1" '+(o==="1"?"selected":"")+'>Altid ja</option>'+
                        '<option value="0" '+(o==="0"?"selected":"")+'>Altid nej</option></select></div>';
                }).join("")+'</div>')+
            '<div style="display:flex;gap:8px;flex-wrap:wrap">'+
            '<button class="fba-send ret-gem" data-uid="'+uid+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">💾 Gem</button>'+
            '<button class="fba-done ret-omdoeb" data-uid="'+uid+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">✏️ Omdøb login</button>'+
            '<button class="fba-done ret-kode" data-uid="'+uid+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">🔑 Nyt kodeord</button>'+
            '<button class="fba-done ret-badg" data-l="'+fbaEsc(p.login)+'" data-n="'+fbaEsc(p.vis||"")+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;background:rgba(60,180,229,.14);color:var(--moerk)" title="Styrer om den interne OGSÅ kan logge ind som borger (universets forside, hub m.m.)">'+
            ((typeof adgangsBorgere==="function"&&adgangsBorgere(true).some(b=>b.u===p.login))?'🧑‍🦽 Borger-adgang: TIL':'🧑‍🦽 Borger-adgang: FRA')+'</button>'+
            (p.ejer?'':'<button class="fba-done ret-toggle" data-uid="'+uid+'" data-til="'+(lukket?1:0)+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;'+(lukket?'background:rgba(147,213,0,.2);color:#41630a':'background:rgba(231,0,76,.12);color:var(--roed)')+'">'+(lukket?'✅ Genåbn adgang':'⛔ Luk adgang')+'</button>'+
                (harLogin?'':'<button class="fba-done ret-slet" data-uid="'+uid+'" style="border:0;border-radius:9px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem;background:rgba(231,0,76,.12);color:var(--roed)">🗑 Fjern profil</button>'))+
            '</div></div></details>';
    }
    html+='<h4 style="margin:16px 0 4px">👥 Brugere med admin-adgang</h4>'+
        '<input id="retSoeg" placeholder="🔍 Søg medarbejder — navn, login, titel eller gruppe…" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:10px;padding:9px 13px;font-family:var(--font);font-size:.9rem;margin:0 0 10px">'+
        (function(){
            const buckets={},ejer=[];
            Object.keys(retData.profiler).forEach(function(uid){const p=retData.profiler[uid];if(p.ejer){ejer.push(uid);return;}const g=p.gruppe||"andre";(buckets[g]=buckets[g]||[]).push(uid);});
            const sortN=function(a,b){return String(retData.profiler[a].vis||retData.profiler[a].login).localeCompare(String(retData.profiler[b].vis||retData.profiler[b].login),'da');};
            const sekt=function(titel,uids,aaben){return uids.length?'<details class="ret-gruppe"'+(aaben?' open':'')+' style="margin:9px 0;background:var(--flade);border-radius:12px;padding:7px 13px">'+
                '<summary style="cursor:pointer;font-weight:800;font-size:.92rem">'+fbaEsc(titel)+' <span style="color:var(--graa);font-weight:400;font-size:.82rem">('+uids.length+')</span></summary>'+
                '<div style="margin-top:6px">'+uids.slice().sort(sortN).map(retBrugerHTML).join("")+'</div></details>':'';};
            let ud2=sekt("👑 Ejer",ejer,false);
            RET_GRP_ORDEN.forEach(function(g){ud2+=sekt(GRP_NAVNE[g]||g,buckets[g]||[],false);delete buckets[g];});
            Object.keys(buckets).forEach(function(g){ud2+=sekt((GRP_NAVNE[g]||g)+" (uden fast gruppe)",buckets[g],false);});
            return ud2;
        })()+
        /* ---- 3) interne UDEN admin-adgang — kan gives adgang med ét klik ---- */
        '<div style="margin:16px 0 8px;padding:12px 14px;background:#fff;border-radius:12px;border:1px dashed rgba(60,180,229,.5)">'+
        '<b>🚀 Aktivér alle medarbejdere på én gang</b>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 8px">Opretter admin-adgang for alle '+MEDARB_DATA.length+' medarbejdere med titel, mail og gruppe ud fra deres rolle — med ét fælles startkodeord. Ved første login TVINGES hver medarbejder til at vælge sit eget kodeord (mindst 8 tegn med bogstav, tal og specialtegn). Allerede aktiverede springes over.</p>'+
        '<button id="retBulk" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">🚀 Aktivér alle</button></div>'+
        '<h4 style="margin:16px 0 4px">🏢 Interne uden admin-adgang — '+udenLogin.length+'</h4>'+
        (udenLogin.length?'<div style="display:grid;gap:6px">'+udenLogin.map(b=>
            '<div class="fba-top" style="padding:8px 12px;background:#fff;border-radius:10px;border:1px solid rgba(30,55,76,.07)">'+
            '<span class="fba-hvem">'+fbaEsc(b.n||"")+' ('+fbaEsc(b.u)+')</span>'+
            '<button class="fba-send ret-giv" data-u="'+fbaEsc(b.u)+'" data-n="'+fbaEsc(b.n||"")+'" style="margin-left:auto;border:0;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem">✅ Giv admin-adgang</button></div>').join("")+'</div>'
            :'<p style="color:var(--graa);font-size:.85rem">Alle interne har adgang.</p>')+
        '<div style="margin-top:20px;padding:14px 16px;background:#fff;border-radius:12px;border:1px solid rgba(231,0,76,.25)">'+
        '<h4 style="margin:0 0 4px">🚫 Ophørte borgere</h4>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:0 0 10px">Borgere hvis adgang er <b>lukket</b> (fx ophørt). De kan slettes permanent — <b style="color:var(--roed)">alt deres data forsvinder uigenkaldeligt</b>. En borger kan kun slettes, når adgangen først er lukket på borgerens kort.</p>'+
        '<div id="ophoertBoks"><p style="color:var(--graa);font-size:.85rem">⏳ Henter …</p></div></div>';
    boks.innerHTML=html;
    renderOphoerte();
    /* ⓘ hjælpetekst pr. rettighed */
    boks.querySelectorAll(".ret-hjaelp").forEach(b=>{
        b.onclick=function(e){ e.preventDefault(); e.stopPropagation(); visRetHjaelp(this,this.getAttribute("data-hp")); };
    });
    /* 🔍 søgning i brugerlisten — filtrér brugere + skjul tomme grupper, auto-udfold ved match */
    const retSoeg=document.getElementById("retSoeg");
    if(retSoeg)retSoeg.oninput=function(){
        const q=(this.value||"").trim().toLowerCase();
        boks.querySelectorAll(".ret-gruppe").forEach(function(gr){
            var vis=0;
            gr.querySelectorAll(".ret-bruger").forEach(function(b){
                var m=!q||(b.getAttribute("data-sog")||"").indexOf(q)>=0;
                b.style.display=m?"":"none"; if(m)vis++;
            });
            gr.style.display=vis?"":"none";
            if(q&&vis)gr.open=true;
        });
    };
    /* gruppe-matrix gem */
    boks.querySelectorAll(".grp-gem").forEach(b=>{
        b.onclick=async function(){
            const g=this.getAttribute("data-g"),permsUd={};
            boks.querySelectorAll('.grp-ret[data-g="'+g+'"]').forEach(c=>{permsUd[c.getAttribute("data-p")]=c.checked?1:0;});
            this.disabled=true;
            try{const r=await apiKald({a:"admgruppegem",token:sess.token,gruppe:g,perms:permsUd});
                if(r&&r.ok){retData.grupper=r.grupper;this.textContent="✔ Gemt";setTimeout(()=>renderRettigheder(),900);}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    /* adgangs-toggle */
    boks.querySelectorAll(".ret-toggle").forEach(b=>{
        b.onclick=async function(){
            const til=this.getAttribute("data-til")==="1";
            if(!confirm(til?"Genåbn admin-adgangen for brugeren?":"Luk admin-adgangen?\nBrugeren logges ud med det samme og kan ikke logge ind, før adgangen genåbnes."))return;
            this.disabled=true;
            try{const r=await apiKald({a:"admtoggle",token:sess.token,uid:this.getAttribute("data-uid"),til:til?1:0});
                if(r&&r.ok){retData=null;renderRettigheder();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    /* kodeordspolitik (samme som serverens): min. 8 tegn m. bogstav, tal og specialtegn */
    const kodePolitikOk=k=>k.length>=8&&/[A-Za-zÆØÅæøå]/.test(k)&&/\d/.test(k)&&/[^A-Za-z0-9ÆØÅæøå]/.test(k);
    const KODE_KRAV="mindst 8 tegn med bogstav, tal og specialtegn";
    /* nyt kodeord */
    boks.querySelectorAll(".ret-kode").forEach(b=>{
        b.onclick=async function(){
            const kode=(window.prompt("Nyt kodeord ("+KODE_KRAV+").\nBrugeren bliver bedt om at vælge sit eget ved næste login:")||"").trim();
            if(!kodePolitikOk(kode)){if(kode)alert("Kodeordet skal være "+KODE_KRAV+".");return;}
            this.disabled=true;
            try{const r=await apiKald({a:"admkode",token:sess.token,uid:this.getAttribute("data-uid"),kodeord:kode});
                if(r&&r.ok){this.textContent="✔ sat";setTimeout(()=>{this.textContent="🔑 Nyt kodeord";this.disabled=false;},1500);}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    /* 🚀 masse-aktivering */
    const bulk=document.getElementById("retBulk");
    if(bulk)bulk.onclick=async function(){
        const kode=(window.prompt("Vælg ét fælles START-kodeord til alle medarbejdere ("+KODE_KRAV+").\nAlle bliver TVUNGET til at vælge deres eget kodeord ved første login:")||"").trim();
        if(!kodePolitikOk(kode)){if(kode)alert("Startkodeordet skal være "+KODE_KRAV+".");return;}
        if(!confirm("Aktivér admin-adgang for alle "+MEDARB_DATA.length+" medarbejdere nu?"))return;
        this.disabled=true;this.textContent="⏳ Opretter …";
        try{
            const r=await apiKald({a:"admbulk",token:sess.token,startkode:kode,
                brugere:MEDARB_DATA.map(m=>({login:m[0],vis:m[0],titel:m[1],mail:m[2],gruppe:m[3]}))});
            if(r&&r.ok){alert("✅ "+r.oprettet+" medarbejdere aktiveret ("+r.sprunget+" havde allerede adgang).");retData=null;renderRettigheder();}
            else{this.disabled=false;this.textContent="🚀 Aktivér alle";alert((r&&r.fejl)||"Kunne ikke aktivere");}
        }catch(e){this.disabled=false;this.textContent="🚀 Aktivér alle";alert("Kunne ikke aktivere — tjek forbindelsen.");}
    };
    /* 🧑‍🦽 borger-adgang til/fra for interne (perm admopret på serveren) */
    boks.querySelectorAll(".ret-badg").forEach(b=>{
        b.onclick=async function(){
            const l=this.getAttribute("data-l");
            const erTil=this.textContent.indexOf("TIL")>=0;
            if(!confirm(erTil?"Luk "+l+"s borger-adgang?\nBrugeren kan stadig logge ind som administrator — men ikke som borger (forside, hub m.m.)."
                :"Giv "+l+" borger-adgang igen?"))return;
            this.disabled=true;
            try{const r=await apiKald({a:"borgeradgangtoggle",token:sess.token,login:l,til:erTil?0:1,navn:this.getAttribute("data-n")});
                if(r&&r.ok){this.textContent="🧑‍🦽 Borger-adgang: "+(r.til?"TIL":"FRA")+" ✔";
                    setTimeout(()=>{this.textContent="🧑‍🦽 Borger-adgang: "+(r.til?"TIL":"FRA");this.disabled=false;},1200);
                    alert("Gemt."+(!r.til&&!r.harAdmKode?"\n\n⚠️ VIGTIGT: "+l+" har endnu IKKE et admin-kodeord — sæt et med 🔑 på kortet her, ellers kan brugeren slet ikke logge ind!":"\nBrugeren logger fremover ind med sit ADMIN-kodeord.")+"\n(Visningen her opdateres helt ved næste side-genindlæsning.)");}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    /* fjern profil uden kodeord (oprydning) */
    boks.querySelectorAll(".ret-slet").forEach(b=>{
        b.onclick=async function(){
            if(!confirm("Fjern profilen helt fra listen?\nHistorik i gamle beskeder beholder navnet — kun rolle og adgang fjernes."))return;
            this.disabled=true;
            try{const r=await apiKald({a:"admprofilslet",token:sess.token,uid:this.getAttribute("data-uid")});
                if(r&&r.ok){retData=null;renderRettigheder();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke fjerne");}}
            catch(e){this.disabled=false;}
        };
    });
    /* giv admin-adgang til intern uden login */
    boks.querySelectorAll(".ret-giv").forEach(b=>{
        b.onclick=async function(){
            const u=this.getAttribute("data-u"),n=this.getAttribute("data-n");
            const kode=(window.prompt("Vælg et start-kodeord til "+(n||u)+" ("+KODE_KRAV+").\nBrugeren tvinges til at vælge sit eget ved første login:")||"").trim();
            if(!kodePolitikOk(kode)){if(kode)alert("Startkodeordet skal være "+KODE_KRAV+".");return;}
            this.disabled=true;
            try{const r=await apiKald({a:"admopretbruger",token:sess.token,login:u,vis:(n||"").split(" ")[0]||u,gruppe:"konsulent",kodeord:kode});
                if(r&&r.ok){retData=null;renderRettigheder();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke oprette");}}
            catch(e){this.disabled=false;}
        };
    });
    boks.querySelectorAll(".ret-gem").forEach(b=>{
        b.onclick=async function(){
            const uid=this.getAttribute("data-uid");
            const over={};
            boks.querySelectorAll('.ret-ov[data-uid="'+uid+'"]').forEach(s=>{over[s.getAttribute("data-p")]=s.value;});
            const grpEl=boks.querySelector('.ret-grp[data-uid="'+uid+'"]');
            const kald={a:"admprofilsaet",token:sess.token,uid:uid,vis:boks.querySelector('.ret-vis[data-uid="'+uid+'"]').value.trim(),over:over,
                titel:(boks.querySelector('.ret-titel[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                mail:(boks.querySelector('.ret-mail[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                foedselsdag:(boks.querySelector('.ret-fd[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                ansat:(boks.querySelector('.ret-ansat[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                mithfadmid:(boks.querySelector('.ret-mid[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                tlf:(boks.querySelector('.ret-tlf[data-uid="'+uid+'"]')||{value:""}).value.trim(),
                direkte:(boks.querySelector('.ret-direkte[data-uid="'+uid+'"]')||{value:""}).value.trim()};
            if(kald.foedselsdag&&!/^\d{2}-\d{2}$/.test(kald.foedselsdag)){alert("Fødselsdag skrives dd-mm, fx 24-06.");this.disabled=false;return;}
            if(kald.ansat&&!/^\d{2}-\d{2}-\d{4}$/.test(kald.ansat)){alert("Ansættelsesdato skrives dd-mm-åååå, fx 01-08-2019.");this.disabled=false;return;}
            if(grpEl)kald.gruppe=grpEl.value;
            this.disabled=true;
            try{const r=await apiKald(kald);
                if(r&&r.ok){retData.profiler[uid]=r.profil;this.textContent="✔ Gemt";setTimeout(()=>{renderRettigheder();},900);}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    boks.querySelectorAll(".ret-omdoeb").forEach(b=>{
        b.onclick=async function(){
            const uid=this.getAttribute("data-uid"),p=retData.profiler[uid];
            const nyt=(window.prompt("Nyt login-brugernavn for "+p.login+":\n(rettigheder, chats og tråde følger automatisk med)",p.login)||"").trim().toLowerCase();
            if(!nyt||nyt===p.login)return;
            if(!confirm("Omdøb «"+p.login+"» til «"+nyt+"»?\n\nVIGTIGT: Du skal bagefter selv rette navnet i admin-brugere.php på serveren, ellers kan brugeren ikke logge ind igen."))return;
            this.disabled=true;
            try{const r=await apiKald({a:"admomdoeb",token:sess.token,uid:uid,nyt:nyt});
                if(r&&r.ok){alert(r.advarsel||"Omdøbt.");retData=null;renderRettigheder();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke omdøbe");}}
            catch(e){this.disabled=false;}
        };
    });
}

/* ===================== 💬 Live-chat (admin: kø, samtaler, åbningstider) ===================== */
let lcaData=null,lcaTimer=null,lcaAaben=null,lcaEfter=0,lcaVist=new Set(),lcaSkrivTs=0;
const LCA_DAGE=[["man","Mandag"],["tir","Tirsdag"],["ons","Onsdag"],["tor","Torsdag"],["fre","Fredag"],["loer","Lørdag"],["soen","Søndag"]];
/* bip/toast/titel-notifikation klares globalt af admin-linjens vagt i univers-retur.js */
function lcaMin(ts){const m=Math.max(0,Math.round((Date.now()/1000-ts)/60));return m<1?"lige nu":m+" min.";}
async function lcaHent(){
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"lcadmin",token:sess.token});return (j&&j.ok)?j:null;}catch(e){return null;}
}
function lcaBadgeOpdater(){
    const el=document.getElementById("lcaBadge");if(!el||!lcaData)return;
    const v=(lcaData.samtaler||[]).filter(s=>s.status==="venter").length,
        a=(lcaData.samtaler||[]).filter(s=>s.status==="aktiv").length,
        m=(lcaData.samtaler||[]).filter(s=>s.nyt).length;
    el.innerHTML=(m?' <span class="antal" style="background:rgba(231,0,76,.2);color:var(--roed)">🆕 '+m+' nyt svar</span>':'')+
        (v?' <span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+v+' venter</span>':'')+
        (a?' <span class="antal">'+a+' i gang</span>':'')+
        ((v||a)?'':' <span style="color:var(--graa);font-weight:400;font-size:.8rem">ingen aktive lige nu</span>');
}
async function renderLiveChat(){
    const panel=document.getElementById("lcPanel"),sess=fbaSess();
    if(!panel||!sess||!sess.token)return;
    if(!document.getElementById("lcaBoks")){   /* skelettet bygges én gang — foldning overlever genrender */
        panel.innerHTML='<details class="fba-kort" id="lcFold" open style="border-left:5px solid var(--blaa)">'+
            '<summary style="cursor:pointer;font-weight:800;font-size:1.02rem">💬 Live-chat<span id="lcaBadge"></span></summary>'+
            '<div id="lcaBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳ Henter …</p></div></details>'+
            '<details class="fba-kort" id="hjulFold" hidden style="border-left:5px solid #f5b400">'+
            '<summary style="cursor:pointer;font-weight:800">🗓 Årshjul <span style="color:var(--graa);font-weight:400;font-size:.8rem">påmindelser til borgerne</span></summary>'+
            '<div id="hjulBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<details class="fba-kort" id="lukFold" hidden style="border-left:5px solid #9db4c4">'+
            '<summary style="cursor:pointer;font-weight:800">🏖 Lukkedage for live-chat <span style="color:var(--graa);font-weight:400;font-size:.8rem">helligdage lukkes automatisk</span></summary>'+
            '<div id="lukBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<details class="fba-kort" id="svarFold" style="border-left:5px solid var(--groen)">'+
            '<summary style="cursor:pointer;font-weight:800">📋 Standardsvar <span style="color:var(--graa);font-weight:400;font-size:.8rem">skriv / i et svarfelt</span></summary>'+
            '<div id="svarBoksPanel" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<details class="fba-kort" id="opretFold" hidden style="border-left:5px solid var(--groen)">'+
            '<summary style="cursor:pointer;font-weight:800">➕ Opret brugere</summary>'+
            '<div id="opretBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<details class="fba-kort" id="videnFold" hidden style="border-left:5px solid var(--blaa)">'+
            '<summary style="cursor:pointer;font-weight:800">📚 Vidensbase <span style="color:var(--graa);font-weight:400;font-size:.8rem">AI-chattens viden — kun ejeren</span></summary>'+
            '<div id="videnBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<div id="assistentWrap" style="display:none">'+
            '<div class="fba-kort" style="border-left:5px solid var(--groen)">'+
            '<b style="font-size:1.02rem">🤝 HF-assistent</b>'+
            '<p style="color:var(--graa);margin:6px 0 12px">Din interne AI-hjælper: spørg om systemer, regler og arbejdsgange. Den svarer ud fra jeres egen vidensbase — og kender ikke persondata. Du kan også bruge den grønne knap nederst til højre.</p>'+
            '<button id="asistAabn" style="border:0;border-radius:12px;padding:11px 18px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.9rem;color:#0d2a12;background:var(--groen)">💬 Åbn HF-assistenten</button>'+
            '</div>'+
            '<details class="fba-kort" id="mvidenFold" hidden style="border-left:5px solid #8a6400">'+
            '<summary style="cursor:pointer;font-weight:800">📚 Assistentens viden <span style="color:var(--graa);font-weight:400;font-size:.8rem">medarbejder-vidensbasen — kun ejeren</span></summary>'+
            '<div id="mvidenBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '</div>'+
            '<div id="tekstFold" style="display:none"><div id="tekstBoks"><p style="color:var(--graa)">⏳</p></div></div>'+
            '<div id="supPanel" style="display:none"><p style="color:var(--graa)">⏳</p></div>'+
            '<details class="fba-kort" id="admRetFold" hidden style="border-left:5px solid #8a6400">'+
            '<summary style="cursor:pointer;font-weight:800">⚙️ Brugere &amp; rettigheder <span style="color:var(--graa);font-weight:400;font-size:.8rem">kun ejeren</span></summary>'+
            '<div id="admRetBoks" style="margin-top:10px"><p style="color:var(--graa)">⏳</p></div></details>'+
            '<div id="admGrupperFold" style="display:none"><div id="admGrupperBoks"><p style="color:var(--graa)">⏳</p></div></div>';
        renderSvarPanel();
    }
    const j=await lcaHent();
    if(!j){if(!lcaData)document.getElementById("lcaBoks").innerHTML='<p style="color:var(--roed)">Live-chatten kunne ikke hentes — kræver serveren.</p>';return;}
    lcaData=j;
    if(j.vismap)ADM_VIS=j.vismap;
    if(j.titelmap)ADM_TITEL=j.titelmap;
    if(j.ejer){const rf=document.getElementById("admRetFold");if(rf&&rf.hidden){rf.hidden=false;renderRettigheder();}
        const lf=document.getElementById("lukFold");if(lf&&lf.hidden){lf.hidden=false;renderLukkedage();}
        const vf=document.getElementById("videnFold");if(vf&&vf.hidden){vf.hidden=false;renderViden();}
        const mvf=document.getElementById("mvidenFold");if(mvf&&mvf.hidden){mvf.hidden=false;renderMViden();}}
    if(j.ret){const af2=document.getElementById("aFab");if(af2)af2.style.display=j.ret.assistent?"grid":"none";}
    if(j.ret&&(j.ret.aarshjul_alle||j.ret.aarshjul_borger)){
        const hf=document.getElementById("hjulFold");if(hf&&hf.hidden){hf.hidden=false;renderHjulAdmin();}}
    if(j.ret&&(j.ret.admopret||j.ret.borgeropret)){
        const of2=document.getElementById("opretFold");if(of2&&of2.hidden){of2.hidden=false;renderOpret(j.ret);}}
    /* områdemenuen: skjul knapper man ikke har ret til + anvend det aktive område */
    const ob=document.getElementById("admOmrBar");
    if(ob&&j.ret){
        const knapRet={fs:j.ret.moderation,hjul:(j.ret.aarshjul_alle||j.ret.aarshjul_borger),stat:j.ret.stat,ret:j.ejer,assistent:j.ret.assistent,tekster:j.ret.tekster,support:j.ret.support,grupper:j.ret.taggrupper};
        ob.querySelectorAll("button[data-o]").forEach(b=>{
            const o2=b.getAttribute("data-o");
            if(knapRet[o2]!==undefined)b.hidden=!knapRet[o2];
        });
        try{sessionStorage.setItem("adm_ret",JSON.stringify(knapRet));}catch(e){}   /* til menuen på Intern/Nyheder-siderne */
    }
    try{   /* kom man via et link, der peger på et bestemt område (fx admin-linjens 💬)? */
        const f2=sessionStorage.getItem("omr_fokus");
        if(f2){sessionStorage.removeItem("omr_fokus");admOmrKlar=true;visOmraade(f2);}
        else if(!admOmrKlar){admOmrKlar=true;visOmraade(admOmr);}
    }catch(e){if(!admOmrKlar){admOmrKlar=true;visOmraade(admOmr);}}
    lcaBadgeOpdater();
    lcaNaevntRender();
    renderDigest();
    const fokus=sessionStorage.getItem("lc_fokus");   /* kom via «Ny besked i din chat»-toast */
    if(fokus){sessionStorage.removeItem("lc_fokus");
        if((j.samtaler||[]).some(x=>x.cid===fokus)){visOmraade("chat");lcaAabnChat(fokus);return;}}
    if(lcaAaben){lcaVisChat();}else{lcaRenderListe();}
    if(!lcaTimer)lcaTimer=setInterval(async ()=>{
        if(location.hash.indexOf("feedback-admin")<0){clearInterval(lcaTimer);lcaTimer=null;return;}
        if(lcaAaben){await lcaPollChat();return;}
        const aktiv=document.activeElement;
        if(aktiv&&(aktiv.tagName==="INPUT"||aktiv.tagName==="TEXTAREA"))return;   /* ikke mens der tastes */
        const nyt=await lcaHent();
        if(!nyt)return;
        const foer=JSON.stringify(lcaData&&lcaData.samtaler)+JSON.stringify(lcaData&&lcaData.online);
        lcaData=nyt;
        lcaBadgeOpdater();
        lcaNaevntRender();
        if(JSON.stringify(nyt.samtaler)+JSON.stringify(nyt.online)!==foer)lcaRenderListe();
    },4000);
}

/* 📥 «Du er nævnt»: samler @tags fra chat-noter, interne tråde og overdragelser */
function lcaNaevntRender(){
    const el=document.getElementById("lcaNaevnt");if(!el||!lcaData)return;
    const liste=lcaData.naevnt||[];
    if(!liste.length){el.innerHTML="";return;}
    el.innerHTML='<div class="fba-kort" style="border-left:5px solid var(--roed)">'+
        '<b>📥 Du er nævnt <span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+liste.length+'</span></b>'+
        liste.slice().reverse().map(e=>'<div class="fba-top" style="margin-top:8px;padding:9px 12px;background:#fff;border-radius:11px;border:1px solid rgba(231,0,76,.14)">'+
            '<span><b>'+fbaEsc(visA(e.af||""))+'</b>'+visTitelHTML(e.af||"")+' · '+(e.kilde==="chat"?"💬 chat":e.kilde==="support"?"📨 support":e.kilde==="isup"?"🎫 intern support":"🗨 intern")+' · '+fbaDato(e.ts||0)+'</span>'+
            '<span style="color:var(--graa);font-style:italic;overflow:hidden;text-overflow:ellipsis;max-width:46ch;white-space:nowrap">«'+fbaEsc(e.txt||"")+'»</span>'+
            '<button class="fba-send lca-nv" data-kilde="'+fbaEsc(e.kilde||"")+'" data-ref="'+fbaEsc(e.ref||"")+'" style="margin-left:auto;border:0;border-radius:9px;padding:7px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.8rem">Åbn</button></div>').join("")+
        '<div style="margin-top:8px"><button id="lcaNvAlle" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.8rem;text-decoration:underline">✓ Markér alle som set</button></div></div>';
    el.querySelectorAll(".lca-nv").forEach(b=>{
        b.onclick=async function(){
            const ref=this.getAttribute("data-ref"),kilde=this.getAttribute("data-kilde");
            try{await apiKald({a:"naevntset",token:fbaSess().token,ref:ref});}catch(e){}
            lcaData.naevnt=(lcaData.naevnt||[]).filter(x=>x.ref!==ref);
            lcaNaevntRender();
            if(kilde==="chat"){visOmraade("chat");lcaAabnChat(ref);}
            else if(kilde==="support"){supAdmSag=ref.replace(/^sup:/,"");visOmraade("support");}
            else if(kilde==="isup"){inTilstand="isup";window.isupFokus=ref.replace(/^isup:/,"");if(location.hash.indexOf("intern")<0){location.hash="#/intern";}else{renderIntern();}}
            else{sessionStorage.setItem("in_fokus",ref);location.hash="#/intern";}
        };
    });
    document.getElementById("lcaNvAlle").onclick=async ()=>{
        try{await apiKald({a:"naevntset",token:fbaSess().token,ref:""});}catch(e){}
        lcaData.naevnt=[];lcaNaevntRender();
    };
}
function lcaRenderListe(){
    const liste=document.getElementById("lcaBoks");if(!liste)return;
    const sess=fbaSess();
    const j=lcaData,klar=!!j.klar,erSoren=!!j.ejer;   /* «erSoren» = ejer-flaget (følger uid, ikke navnet) */
    sessionStorage.setItem("lc_klar",klar?"1":"0");
    const alle=(j.samtaler||[]).slice().sort((a,b)=>b.sidst-a.sidst);
    const venter=alle.filter(s=>s.status==="venter"),igang=alle.filter(s=>s.status==="aktiv"),arkiv=alle.filter(s=>s.status==="arkiv");
    const hvem=s=>'<span data-bku="'+fbaEsc(s.u)+'" title="Åbn borger-kortet">'+(s.n?fbaEsc(s.n)+" ("+fbaEsc(s.u)+")":fbaEsc(s.u))+'</span>';
    /* status-kort */
    let html='<div class="fba-kort" style="display:flex;gap:14px;align-items:center;flex-wrap:wrap;border-left:5px solid '+(klar?'var(--groen)':'#9db4c4')+'">'+
        '<span style="font-size:1.5rem">'+(klar?'🟢':'⚫')+'</span>'+
        '<div style="flex:1;min-width:220px"><b>Jeg tager imod chats: '+(klar?'Ja':'Nej')+'</b>'+
        '<div style="color:var(--graa);font-size:.85rem">Online nu: '+((j.online||[]).length?fbaEsc(j.online.map(visA).join(", ")):"ingen")+
        ' · Åbningstid lige nu: '+(j.aabenNu?"åben":"lukket")+
        ((j.aabenNu&&(j.online||[]).length)?'':' — borgerne kan IKKE se chat-knappen lige nu')+'</div></div>'+
        '<button id="lcaKlar" class="'+(klar?'fba-done':'fba-send')+'" style="border:0;border-radius:12px;padding:11px 18px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.87rem">'+
        (klar?'Slå fra':'🟢 Slå til')+'</button></div>';
    /* åbningstider */
    const t=j.tider||{};
    html+='<details class="fba-kort"'+(erSoren?'':' ')+'><summary style="cursor:pointer;font-weight:800">🕘 Åbningstider for medarbejder-chat'+(erSoren?'':' (kun ejeren kan ændre)')+'</summary>'+
        '<div style="display:grid;gap:6px;margin-top:10px">'+
        LCA_DAGE.map(d=>{const r=t[d[0]]||{on:0,fra:"09:00",til:"15:00"};
            return '<div style="display:flex;gap:10px;align-items:center">'+
                '<label style="width:110px;display:flex;gap:8px;align-items:center;font-weight:600"><input type="checkbox" class="lca-dag" data-d="'+d[0]+'" '+(r.on?"checked":"")+(erSoren?"":" disabled")+'> '+d[1]+'</label>'+
                '<input type="time" class="lca-fra" data-d="'+d[0]+'" value="'+fbaEsc(r.fra)+'"'+(erSoren?"":" disabled")+' style="font-family:var(--font);padding:5px 8px;border:1px solid rgba(30,55,76,.18);border-radius:8px"> – '+
                '<input type="time" class="lca-til" data-d="'+d[0]+'" value="'+fbaEsc(r.til)+'"'+(erSoren?"":" disabled")+' style="font-family:var(--font);padding:5px 8px;border:1px solid rgba(30,55,76,.18);border-radius:8px">'+
                '</div>';}).join("")+
        (erSoren?'<button id="lcaTiderGem" class="fba-send" style="border:0;border-radius:10px;padding:10px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem;margin-top:4px">💾 Gem åbningstider</button>':'')+
        '</div></details>';
    /* venter */
    html+='<div class="fba-kort" style="border-left:5px solid '+(venter.length?'var(--roed)':'rgba(30,55,76,.15)')+'">'+
        '<b>⏳ Venter på en medarbejder <span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+venter.length+'</span></b>'+
        (venter.length?venter.map(s=>'<div class="fba-top" style="margin-top:10px;padding:10px 12px;background:#fff;border-radius:12px;border:1px solid rgba(231,0,76,.15)">'+
                '<span class="fba-hvem">'+hvem(s)+'</span><span style="color:var(--graa)">ventet '+lcaMin(s.start)+'</span>'+
                (s.seneste?'<span style="color:var(--graa);font-style:italic">«'+fbaEsc(s.seneste)+'»</span>':'')+
                '<button class="fba-send lca-tag" data-cid="'+fbaEsc(s.cid)+'" style="margin-left:auto;border:0;border-radius:10px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.84rem">📞 Tag chatten</button></div>').join("")
            :'<p style="color:var(--graa);margin:8px 0 0">Ingen venter 🎉</p>')+'</div>';
    /* i gang */
    html+='<div class="fba-kort"><b>💬 I gang <span class="antal">'+igang.length+'</span></b>'+
        (igang.length?igang.map(s=>{const mig=sess&&s.adm===sess.u;
                return '<div class="fba-top" style="margin-top:10px;padding:10px 12px;background:#fff;border-radius:12px;border:1px solid '+(s.nyt?'rgba(231,0,76,.45)':'rgba(30,55,76,.08)')+'">'+
                    '<span class="fba-hvem">'+hvem(s)+'</span>'+
                    (s.nyt?'<span class="fba-side" style="background:rgba(231,0,76,.16);color:var(--roed)">🆕 nyt svar</span>':'')+
                    '<span class="fba-side" style="'+(mig?'background:rgba(147,213,0,.2);color:#41630a':'')+'">'+(mig?'✋ hos dig':'hos '+fbaEsc(visA(s.adm)))+'</span>'+
                    '<span style="color:var(--graa)">seneste aktivitet '+lcaMin(s.sidst)+'</span>'+
                    '<button class="fba-done lca-aabn" data-cid="'+fbaEsc(s.cid)+'" style="margin-left:auto">'+(mig?'💬 Åbn chatten':'👁 Kig med')+'</button></div>';}).join("")
            :'<p style="color:var(--graa);margin:8px 0 0">Ingen aktive samtaler.</p>')+'</div>';
    /* arkiv — grupperet pr. bruger */
    const agrp={};arkiv.forEach(s=>{(agrp[s.u]=agrp[s.u]||[]).push(s);});
    const abrugere=Object.keys(agrp).sort((a,b)=>Math.max(...agrp[b].map(s=>s.sidst))-Math.max(...agrp[a].map(s=>s.sidst)));
    html+='<details class="fba-kort"><summary style="cursor:pointer;font-weight:800">🗄 Arkiv — '+arkiv.length+' afsluttede</summary>'+
        (abrugere.length?abrugere.map(u=>{const g=agrp[u].sort((a,b)=>b.sidst-a.sidst);
                return '<details style="margin:8px 0 0 6px"><summary style="cursor:pointer;font-weight:700">'+hvem(g[0])+' — '+g.length+'</summary>'+
                    g.map(s=>'<div class="fba-top" style="margin:6px 0;padding:8px 12px;background:#fff;border-radius:10px;border:1px solid rgba(30,55,76,.06)">'+
                        '<span>'+fbaDato(s.start)+'</span><span style="color:var(--graa)">💬 '+s.antal+(s.overfoert?' · ↪ overført til beskeder':'')+(s.adm?' · '+fbaEsc(visA(s.adm)):'')+'</span>'+
                        '<button class="fba-done lca-aabn" data-cid="'+fbaEsc(s.cid)+'" style="margin-left:auto">📜 Se udskrift</button></div>').join("")+'</details>';}).join("")
            :'<p style="color:var(--graa);margin:8px 0 0">Tomt endnu.</p>')+'</details>';
    liste.innerHTML=html;
    /* lyttere */
    document.getElementById("lcaKlar").onclick=async function(){
        this.disabled=true;
        try{const r=await apiKald({a:"lconline",token:sess.token,klar:klar?0:1});
            if(r&&r.ok){
                sessionStorage.setItem("lc_klar",r.klar?"1":"0");
                lcaData.klar=r.klar;lcaData.online=r.online;lcaRenderListe();
                const ab=document.getElementById("abKlar");   /* opdatér også admin-linjens chip med det samme */
                if(ab){ab.textContent=r.klar?"🟢 Tager imod chats":"⚫ Ikke klar til chat";ab.classList.toggle("gr",!!r.klar);}
            }}
        catch(e){this.disabled=false;}
    };
    const gemT=document.getElementById("lcaTiderGem");
    if(gemT)gemT.onclick=async function(){
        const dage={};
        LCA_DAGE.forEach(d=>{
            dage[d[0]]={on:liste.querySelector('.lca-dag[data-d="'+d[0]+'"]').checked?1:0,
                fra:liste.querySelector('.lca-fra[data-d="'+d[0]+'"]').value||"09:00",
                til:liste.querySelector('.lca-til[data-d="'+d[0]+'"]').value||"15:00"};
        });
        this.disabled=true;
        try{const r=await apiKald({a:"lctider",token:sess.token,dage:dage});
            if(r&&r.ok){lcaData.tider=r.tider;this.textContent="✔ Gemt";setTimeout(()=>{this.textContent="💾 Gem åbningstider";this.disabled=false;},1500);}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;}
    };
    liste.querySelectorAll(".lca-tag").forEach(b=>{
        b.onclick=async function(){
            this.disabled=true;
            try{const r=await apiKald({a:"lctag",token:sess.token,cid:this.getAttribute("data-cid")});
                if(r&&r.ok){
                    sessionStorage.setItem("lc_klar","1");
                    const nyt=await lcaHent();if(nyt)lcaData=nyt;   /* friske data FØR visning — ellers ser man «Tag chatten» igen */
                    lcaAabnChat(this.getAttribute("data-cid"));
                }
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke tage chatten");renderLiveChat();}}
            catch(e){this.disabled=false;}
        };
    });
    liste.querySelectorAll(".lca-aabn").forEach(b=>{b.onclick=()=>lcaAabnChat(b.getAttribute("data-cid"));});
}
function lcaAabnChat(cid){lcaAaben=cid;lcaVisChat();}
function lcaVisChat(){
    /* loggen gen-tegnes fra bunden hver gang → nulstil altid dedup-tilstanden,
     ellers bliver gamle beskeder væk efter fx «Tag chatten»-gentegning */
    lcaEfter=0;lcaVist.clear();
    const liste=document.getElementById("lcaBoks");if(!liste)return;
    const sess=fbaSess();
    const s=(lcaData.samtaler||[]).find(x=>x.cid===lcaAaben);
    if(!s){lcaAaben=null;lcaRenderListe();return;}
    const mig=sess&&s.adm===sess.u,aktivMin=s.status==="aktiv"&&mig;
    liste.innerHTML=
        '<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="display:flex;gap:12px;align-items:center;padding:14px 18px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff">'+
        '<button id="lcaTilbage" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:9px;padding:7px 12px;cursor:pointer;font-family:var(--font);font-weight:700">‹ Oversigt</button>'+
        '<div><b data-bku="'+fbaEsc(s.u)+'" title="Åbn borger-kortet">'+(s.n?fbaEsc(s.n):fbaEsc(s.u))+'</b><div style="opacity:.75;font-size:.78rem">'+fbaEsc(s.u)+
        (s.status==="aktiv"?' · hos '+fbaEsc(visA(s.adm)):s.status==="venter"?' · venter':' · afsluttet')+'</div></div>'+
        '<div style="margin-left:auto;display:flex;gap:8px;flex-wrap:wrap">'+
        (s.status==="venter"?'<button id="lcaTagH" class="fba-send" style="border:0;border-radius:10px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.84rem">📞 Tag chatten</button>':'')+
        (aktivMin?'<button id="lcaSlip" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:10px;padding:9px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem" title="Chatten ryger tilbage i køen">↩ Giv slip</button>'+
            '<button id="lcaGiv" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:10px;padding:9px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem" title="Overdrag direkte til en kollega">→ Overdrag</button>'+
            '<button id="lcaArkiv" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:10px;padding:9px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">🗄 Afslut</button>':'')+
        '</div></div>'+
        '<div id="lcaGivBoks" style="display:none;gap:10px;align-items:center;padding:10px 18px;background:rgba(60,180,229,.1);border-bottom:1px solid rgba(60,180,229,.25);font-size:.85rem"></div>'+
        '<div id="lcaLog" style="max-height:440px;overflow-y:auto;padding:16px;display:flex;flex-direction:column;gap:8px;background:var(--flade)"></div>'+
        '<div id="lcaSkrivInfo" style="display:none;color:var(--graa);font-size:.8rem;padding:4px 18px">✍️ Borgeren skriver …</div>'+
        (aktivMin?'<div style="display:flex;gap:10px;padding:12px 16px;border-top:1px solid rgba(30,55,76,.08);background:#fff">'+
            '<textarea id="lcaInput" rows="1" placeholder="Skriv dit svar … (/ = standardsvar)" style="flex:1;border:2px solid #dbe6ee;border-radius:18px;padding:11px 16px;font-family:var(--font);font-size:.93rem;outline:none;resize:none;line-height:1.35;max-height:120px;overflow-y:auto"></textarea>'+
            hfEmojiKnap("lcaInput")+
            '<button id="lcaAI" title="Lav et AI-udkast ud fra samtalen og vidensbasen — du redigerer selv" style="border:0;background:rgba(60,180,229,.14);color:var(--moerk);border-radius:50%;width:44px;height:44px;flex:none;cursor:pointer;font-size:1rem">✍️</button>'+
            '<button id="lcaSend" class="fba-send" style="border:0;border-radius:50%;width:44px;height:44px;flex:none;cursor:pointer;font-size:1.05rem">➤</button></div>':'')+
        /* 🔒 interne noter — synlige for alle admins, aldrig for borgeren */
        '<div style="background:#fff8e1;border-top:2px dashed rgba(200,160,40,.55);padding:12px 18px">'+
        '<div style="font-size:.78rem;font-weight:800;color:#8a6400;letter-spacing:.04em">🔒 INTERNE NOTER — borgeren ser aldrig dette</div>'+
        '<div id="lcaNoteListe" style="margin:8px 0 10px"></div>'+
        '<div style="display:flex;gap:8px;position:relative">'+
        '<input id="lcaNoteFelt" type="text" maxlength="1000" placeholder="Skriv internt notat … @navn tagger en kollega" style="flex:1;border:1px solid rgba(200,160,40,.45);background:#fff;border-radius:10px;padding:9px 13px;font-family:var(--font);font-size:.85rem;outline:none">'+
        '<button id="lcaNoteGem" style="border:0;background:#8a6400;color:#fff;border-radius:10px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">Gem</button>'+
        '<div id="lcaNoteForslag" style="position:absolute;bottom:calc(100% + 4px);left:0;display:none;gap:6px;flex-wrap:wrap;background:#fff;border-radius:10px;box-shadow:var(--skygge);padding:6px;z-index:5"></div>'+
        '</div></div>'+
        '</div>';
    document.getElementById("lcaTilbage").onclick=()=>{lcaAaben=null;renderLiveChat();};
    const tagH=document.getElementById("lcaTagH");
    if(tagH)tagH.onclick=async function(){
        this.disabled=true;
        try{const r=await apiKald({a:"lctag",token:sess.token,cid:lcaAaben});
            if(r&&r.ok){sessionStorage.setItem("lc_klar","1");const nyt=await lcaHent();if(nyt)lcaData=nyt;lcaVisChat();}
            else{alert((r&&r.fejl)||"Kunne ikke tage chatten");this.disabled=false;}}
        catch(e){this.disabled=false;}
    };
    const ark=document.getElementById("lcaArkiv");
    if(ark)ark.onclick=async function(){
        if(!confirm("Afslut chatten og læg den i arkivet?"))return;
        this.disabled=true;
        try{const r=await apiKald({a:"lcarkiv",token:sess.token,cid:lcaAaben});
            if(r&&r.ok){lcaAaben=null;const nyt=await lcaHent();if(nyt)lcaData=nyt;lcaRenderListe();}else this.disabled=false;}
        catch(e){this.disabled=false;}
    };
    /* ↩ giv slip: chatten ryger tilbage i køen, en kollega kan tage den */
    const slip=document.getElementById("lcaSlip");
    if(slip)slip.onclick=async function(){
        if(!confirm("Giv slip på chatten?\nDen ryger tilbage i Venter-køen, og borgeren får besked om at blive stillet videre."))return;
        this.disabled=true;
        try{const r=await apiKald({a:"lcslip",token:sess.token,cid:lcaAaben});
            if(r&&r.ok){lcaAaben=null;const nyt=await lcaHent();if(nyt)lcaData=nyt;lcaRenderListe();}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke give slip");}}
        catch(e){this.disabled=false;}
    };
    /* → overdrag: vælg en online kollega */
    const giv=document.getElementById("lcaGiv"),givBoks=document.getElementById("lcaGivBoks");
    if(giv)giv.onclick=function(){
        const kolleger=(lcaData.online||[]).filter(n=>n!==sess.u);
        if(!kolleger.length){alert("Ingen andre administratorer er online lige nu — brug «Giv slip» i stedet.");return;}
        givBoks.style.display="flex";
        givBoks.innerHTML='Overdrag chatten til: <select id="lcaGivValg" style="font-family:var(--font);padding:7px 10px;border-radius:9px;border:1px solid rgba(30,55,76,.2)">'+
            kolleger.map(n=>'<option value="'+fbaEsc(n)+'">'+fbaEsc(visA(n))+'</option>').join("")+'</select>'+
            '<button id="lcaGivOk" class="fba-send" style="border:0;border-radius:9px;padding:8px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">Overdrag</button>'+
            '<button id="lcaGivNej" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.83rem">Annullér</button>';
        document.getElementById("lcaGivNej").onclick=()=>{givBoks.style.display="none";};
        document.getElementById("lcaGivOk").onclick=async function(){
            this.disabled=true;
            try{const r=await apiKald({a:"lcgiv",token:sess.token,cid:lcaAaben,til:document.getElementById("lcaGivValg").value});
                if(r&&r.ok){lcaAaben=null;const nyt=await lcaHent();if(nyt)lcaData=nyt;lcaRenderListe();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke overdrage");}}
            catch(e){this.disabled=false;}
        };
    };
    /* 🔒 interne noter */
    const noteFelt=document.getElementById("lcaNoteFelt"),noteGem=document.getElementById("lcaNoteGem");
    atForslag(noteFelt,document.getElementById("lcaNoteForslag"),GRP_TAG_LISTE.concat(lcaData.admins||[]));
    async function gemNote(){
        const v=noteFelt.value.trim();if(!v)return;
        noteGem.disabled=true;
        try{const r=await apiKald({a:"lcnotat",token:sess.token,cid:lcaAaben,txt:v});
            if(r&&r.ok){noteFelt.value="";lcaNoterCache.push(r.notat);lcaNoterRender();}
            else alert((r&&r.fejl)||"Kunne ikke gemme notatet");}
        catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");}
        noteGem.disabled=false;
    }
    noteGem.onclick=gemNote;
    noteFelt.addEventListener("keydown",e=>{if(e.key==="Enter")gemNote();});
    lcaNoterCache=[];lcaNoterRender();
    const inp2=document.getElementById("lcaInput"),snd=document.getElementById("lcaSend");
    async function sendAdm(){
        const v=inp2.value.trim();if(!v)return;
        inp2.value="";inp2.style.height="auto";
        lcaTilfoejBoble({fra:"adm",af:sess.u,txt:v,ts:Math.floor(Date.now()/1000)},true);
        try{const r=await apiKald({a:"lcadmsend",token:sess.token,cid:lcaAaben,txt:v});
            if(!(r&&r.ok))alert((r&&r.fejl)||"Beskeden kom ikke frem");}
        catch(e){alert("Beskeden kom ikke frem — tjek forbindelsen.");}
    }
    if(snd){snd.onclick=sendAdm;
        inp2.addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey&&!inp2.value.trim().startsWith("/")){e.preventDefault();sendAdm();}});
        inp2.addEventListener("input",()=>hfGrow(inp2,120));
        inp2.addEventListener("input",()=>{
            if(!inp2.value.trim()||inp2.value.startsWith("/"))return;
            const nu=Date.now();
            if(nu-lcaSkrivTs>4000){lcaSkrivTs=nu;apiSend({a:"lcskriv",token:sess.token,cid:lcaAaben});}
        });
        svarForslagOpsæt(inp2,()=>(s.n||"").split(" ")[0]);   /* «/» = standardsvar */
        const ai=document.getElementById("lcaAI");
        if(ai)ai.onclick=async function(){
            this.disabled=true;this.textContent="⏳";
            try{const r=await apiKald({a:"aiudkast",token:sess.token,cid:lcaAaben});
                if(r&&r.ok){inp2.value=r.udkast;inp2.focus();}else alert((r&&r.fejl)||"Kunne ikke lave udkast");}
            catch(e){alert("Kunne ikke lave udkast — tjek forbindelsen.");}
            this.disabled=false;this.textContent="✍️";
        };
    }
    lcaPollChat();
}

/* @-forslag: skriv @ i et felt → klikbare navne (bruges i chat-noter og intern).
   Genvej: er der kun ÉT navn tilbage på listen, fuldfører MELLEMRUM det automatisk. */
function atForslag(felt,boks,navne){
    if(!felt||!boks)return;
    function luk(){boks.innerHTML="";boks.style.display="none";}
    function kandidater(){
        const m=/@([a-zæøå0-9_\-\.]*)$/i.exec(felt.value);
        if(!m)return null;
        const sog=m[1].toLowerCase();
        /* match hvis søgeteksten er start på HELE brugernavnet, eller på et ORD (for-/efternavn)
       i enten brugernavnet eller vis-navnet — så man kan tagge på efternavn også */
        return (navne||[]).filter(function(n){
            const nl=n.toLowerCase();
            if(nl===sog)return false;
            const vis=(typeof visA==="function"?String(visA(nl)):n).toLowerCase();
            return nl.indexOf(sog)===0 || (nl+" "+vis).split(/[\s\-]+/).some(function(w){return w.indexOf(sog)===0;});
        }).slice(0,6);
    }
    function indsaet(navn){
        felt.value=felt.value.replace(/@([a-zæøå0-9_\-\.]*)$/i,"@"+navn+" ");
        luk();felt.focus();
    }
    felt.addEventListener("keydown",e=>{
        if(e.key!==" ")return;
        const hits=kandidater();
        if(hits&&hits.length===1){e.preventDefault();indsaet(hits[0]);}   /* ét navn tilbage → mellemrum vælger det */
    });
    felt.addEventListener("input",()=>{
        const hits=kandidater();
        if(!hits||!hits.length){luk();return;}
        boks.innerHTML=hits.map(n=>'<button type="button" data-n="'+fbaEsc(n)+'" style="border:1px solid '+(GRP_TAGS_KLIENT[n.toLowerCase()]?'rgba(61,122,0,.5)':'rgba(60,180,229,.4)')+';background:#fff;color:var(--moerk);border-radius:999px;padding:5px 11px;cursor:pointer;font-family:var(--font);font-size:.8rem;font-weight:700">@'+fbaEsc(n)+
            (GRP_TAGS_KLIENT[n.toLowerCase()]?' <span style="opacity:.7;font-weight:400">👥 hele gruppen</span>':
                (visA(n)!==n?' <span style="opacity:.6;font-weight:400">'+fbaEsc(visA(n))+'</span>':''))+
            (hits.length===1?' <span style="opacity:.55;font-weight:400">␣</span>':'')+'</button>').join("");
        boks.style.display="flex";
        boks.querySelectorAll("button").forEach(b=>{b.onclick=()=>indsaet(b.getAttribute("data-n"));});
    });
    felt.addEventListener("blur",()=>setTimeout(luk,250));
}

/* interne noter: render m. fremhævede @tags */
let lcaNoterCache=[];
function lcaNoteHTML(nt){
    const t=inTagHTML(nt.txt||"");
    return '<div style="background:#fff;border:1px solid rgba(200,160,40,.3);border-radius:10px;padding:8px 12px;margin-top:6px;font-size:.85rem">'+
        '<span style="font-weight:800">'+fbaEsc(visA(nt.af||""))+visTitelHTML(nt.af||"")+'</span> <span style="color:var(--graa);font-size:.75rem">'+fbaDato(nt.ts||0)+'</span><br>'+t+'</div>';
}
function lcaNoterRender(){
    const el=document.getElementById("lcaNoteListe");if(!el)return;
    el.innerHTML=lcaNoterCache.length?lcaNoterCache.map(lcaNoteHTML).join("")
        :'<span style="color:var(--graa);font-size:.8rem">Ingen noter endnu.</span>';
}
function lcaTilfoejBoble(b,egen){
    const log2=document.getElementById("lcaLog");if(!log2)return;
    const nkey=(b.ts||0)+"|"+b.fra+"|"+(b.txt||"");
    if(!egen&&lcaVist.has(nkey))return;
    lcaVist.add(nkey);
    const d=document.createElement("div");
    if(b.fra==="system"){d.style.cssText="align-self:center;color:var(--graa);font-size:.78rem;text-align:center";}
    else if(b.fra==="adm"){d.style.cssText="align-self:flex-end;background:var(--moerk);color:#fff;padding:9px 13px;border-radius:15px;border-bottom-right-radius:5px;max-width:80%;font-size:.9rem;white-space:pre-line";}
    else{d.style.cssText="align-self:flex-start;background:#fff;box-shadow:var(--skygge);padding:9px 13px;border-radius:15px;border-bottom-left-radius:5px;max-width:80%;font-size:.9rem;white-space:pre-line";}
    d.textContent=(b.fra==="adm"&&b.af?visA(b.af)+": ":"")+(b.txt||"");
    log2.appendChild(d);log2.scrollTop=log2.scrollHeight;
}
async function lcaPollChat(){
    if(!lcaAaben)return;
    const sess=fbaSess();if(!sess)return;
    const genind=lcaEfter===0;   /* første hentning → vis også egne gamle svar */
    try{
        const j=await apiKald({a:"lchentadm",token:sess.token,cid:lcaAaben,efter:lcaEfter});
        if(!(j&&j.ok))return;
        (j.beskeder||[]).forEach(b=>{
            lcaEfter=Math.max(lcaEfter,b.ts||0);
            if(b.fra==="adm"&&b.af===sess.u&&!genind)return;   /* egne svar vises straks ved afsendelse */
            lcaTilfoejBoble(b,false);
        });
        const info=document.getElementById("lcaSkrivInfo");
        if(info)info.style.display=j.skriver?"block":"none";
        /* interne noter: opdatér når der er nyt (fx en kollega skrev et notat) */
        if(j.notater&&JSON.stringify(j.notater)!==JSON.stringify(lcaNoterCache)){lcaNoterCache=j.notater;lcaNoterRender();}
        /* status ændret (fx borgeren afsluttede) → opdatér top-baren ved næste fulde hent */
    }catch(e){}
}

/* ===================== 🗨 Intern kommunikation (kun administratorer) ===================== */
let inData=null,inRumValgt="gen",inTraad=null,inTimer=null,inEfter=0,inVisArkiv=false,inOpretVis=false,inTikTael=0,inMaaRed=false,inMaaMod=false;
var inLaestTil=0,inBeskeder=[],tfOpen=false;   /* «læst til her»-fold i tråde/kanaler */
var beskedMrk={},beskedUdvid=new Set();   /* enkelt-post «læst»/«behandlet»-markering + midlertidigt udfoldede */
var inSoeg="",inFilterDelt="",inKunUlaest=false,inKunNaevnt=false,kanSoeg="";   /* søgning/filtrering i tråd-/kanal-lister */
var inSoegTraf=[],inSoegTimer=null;   /* #6: tråd-id'er der matcher i selve beskedteksten (server-søgning) */
var kanFiltAf="",kanKunNaevnt=false,kanMsgSoeg="";   /* #3/#4: filtrér beskeder INDE i en kanal */
var kanListeNaevnt=false,kanListeMedlem="";   /* #3/#4: filtrér kanal-LISTEN */
/* @gruppe-tags: tag en HEL rettigheds-gruppe (fx @konsulenter) — foldes ud på serveren */
const GRP_TAGS_KLIENT={direktionen:"direktion",ledere:"leder",konsulenter:"konsulent",hr:"hr","løn":"loen",bookere:"booker"};
const GRP_TAG_LISTE=Object.keys(GRP_TAGS_KLIENT);
function inTagHTML(t){
    /* match HELE det kendte brugernavn (også med mellemrum, fx «vicky dose») → vis KUN vis-navnet i blåt.
     Ukendte @ord highlightes stadig som før. Kendte navne prøves længst-først, så flerords-navne vinder. */
    const kendte=GRP_TAG_LISTE.concat(Object.keys(ADM_VIS)).filter(Boolean).sort((a,b)=>b.length-a.length);
    const escNavn=n=>fbaEsc(n).replace(/[.*+?^${}()|[\]\\]/g,"\\$&");
    const alt=kendte.map(escNavn).join("|");
    const re=new RegExp("@(?:"+(alt?alt+"|":"")+"[a-zæøå0-9_.\\-]+)","gi");
    return fbaEsc(t).replace(re,m=>{
        const navn=m.slice(1),lo=navn.toLowerCase();
        if(GRP_TAGS_KLIENT[lo])return '<b style="color:#3d7a00">@'+fbaEsc((typeof GRP_NAVNE!=="undefined"&&GRP_NAVNE[GRP_TAGS_KLIENT[lo]])||navn)+' 👥</b>';
        return '<b style="color:var(--blaa)">@'+fbaEsc(visA(lo))+'</b>';
    });
}

/* ===================== 🎫 INTERN SUPPORT — servicedesk mellem medarbejdere ===================== */
var isupData=null,isupSag=null,isupVis="tilmig",isupFilter={q:"",gruppe:"",kat:"",prio:"",status:"aaben"},isupOpretVis=false,isupKatVis=false;
if(typeof window.isupFokus==="undefined")window.isupFokus="";
var ISUP_PRIO={normal:{n:"Normal",f:"#9db4c4"},hoej:{n:"Høj",f:"#e0a800"},haster:{n:"Haster",f:"#e7004c"}};
function isupPrioDot(p){var m=ISUP_PRIO[p]||ISUP_PRIO.normal;return '<span title="'+m.n+'" style="display:inline-block;width:11px;height:11px;border-radius:50%;background:'+m.f+';flex:0 0 auto"></span>';}
function isupStatusBadge(s,tv){if(s==="loest")return '<span style="background:#e6f4ea;color:#1f7a45;border-radius:999px;font-size:.72rem;font-weight:800;padding:2px 9px">✔ Løst</span>';if(tv)return '<span style="background:#eaf3fb;color:#2a5a78;border-radius:999px;font-size:.72rem;font-weight:800;padding:2px 9px">👤 '+fbaEsc(tv)+'</span>';return '<span style="background:#fff3e0;color:#8a5a00;border-radius:999px;font-size:.72rem;font-weight:800;padding:2px 9px">⏳ I kø</span>';}
function isupFrist(ts){if(!ts)return "";var nu=Date.now()/1000,d=ts-nu,dag=86400,f,t;if(d<0){f="#e7004c";t="forfaldt";}else if(d<dag){f="#e0653a";t="i dag";}else if(d<3*dag){f="#c68400";t="om "+Math.ceil(d/dag)+" dage";}else{f="#7a8a97";t=new Date(ts*1000).toLocaleDateString("da-DK",{day:"2-digit",month:"short"});}return '<span style="display:inline-block;background:'+f+'1f;color:'+f+';border:1px solid '+f+'55;border-radius:999px;font-size:.72rem;font-weight:700;padding:1px 8px">⏰ '+t+'</span>';}
function isupTxt(t){return fbaEsc(t).replace(/@([A-Za-z0-9ÆØÅæøåÄÖäöü._-]+)/g,'<span style="color:var(--blaa);font-weight:700">@$1</span>').replace(/\b(\d{6})\b/g,'<span class="isup-bku" data-bku="$1" style="color:var(--blaa);font-weight:700;cursor:pointer;text-decoration:underline">$1</span>');}
function isupBkuWire(root){root.querySelectorAll(".isup-bku").forEach(function(x){x.onclick=function(e){e.stopPropagation();if(typeof aabnBorgerKort==="function")aabnBorgerKort(x.getAttribute("data-bku"));};});}
function isupModtagerOpts(vm,grupper,valgt){
    var g='<optgroup label="Grupper">'+Object.keys(grupper||{}).map(function(k){return '<option value="gruppe:'+fbaEsc(k)+'"'+(valgt==="gruppe:"+k?" selected":"")+'>👥 '+fbaEsc(grupper[k])+'</option>';}).join("")+'</optgroup>';
    var folk=Object.keys(vm||{}).map(function(l){return {l:l,n:vm[l]||l};}).sort(function(a,b){return String(a.n).localeCompare(String(b.n),"da");});
    return g+'<optgroup label="Medarbejdere">'+folk.map(function(f){return '<option value="person:'+fbaEsc(f.l)+'"'+(valgt==="person:"+f.l?" selected":"")+'>'+fbaEsc(f.n)+'</option>';}).join("")+'</optgroup>';
}
async function isupStart(){
    var wrap=document.getElementById("inWrap"),sess=fbaSess();if(!wrap||!sess)return;
    if(!isupData){wrap.innerHTML=inFaneHTML()+'<p style="color:var(--graa)">⏳ Henter interne sager …</p>';inFaneLyt(wrap);}
    try{var j=await apiKald({a:"isupliste",token:sess.token});if(j&&j.ok){isupData=j;window.hfIsupBadge=j.antalTilMig||0;}}catch(e){}
    if(inTilstand!=="isup"||location.hash.indexOf("intern")<0)return;
    if(window.isupFokus){var id=window.isupFokus;window.isupFokus="";isupVisSag(id);return;}
    if(isupSag){isupVisSag(isupSag);return;}
    isupRenderListe();
}
function isupKortHTML(s){
    var pm=ISUP_PRIO[s.prioritet]||ISUP_PRIO.normal;
    return '<div class="isup-kort" data-id="'+fbaEsc(s.id)+'" style="background:#fff;border:1px solid rgba(30,55,76,.08);border-left:4px solid '+pm.f+';border-radius:14px;padding:12px 14px;cursor:pointer;box-shadow:var(--skygge);display:flex;flex-direction:column;gap:6px'+(s.nyt?';box-shadow:0 0 0 2px rgba(231,0,76,.12)':'')+'">'+
        '<div style="display:flex;align-items:center;gap:8px">'+isupPrioDot(s.prioritet)+
        '<b style="flex:1;min-width:0;color:var(--moerk);font-size:.96rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+isupTxt(s.emne)+'</b>'+
        (s.nyt?'<span style="width:9px;height:9px;border-radius:50%;background:#e7004c;flex:0 0 auto" title="nyt"></span>':'')+'</div>'+
        '<div style="display:flex;align-items:center;gap:7px;flex-wrap:wrap;font-size:.77rem;color:var(--graa)">'+
        (s.kategori?'<span style="background:var(--flade);border:1px solid rgba(30,55,76,.1);border-radius:6px;padding:1px 7px">'+fbaEsc(s.kategori)+'</span>':'')+
        isupStatusBadge(s.status,s.tildeltVis)+(s.frist?isupFrist(s.frist):'')+
        '<span style="margin-left:auto">'+fbaEsc(s.afVis)+' → '+(s.modtagerType==="gruppe"?'👥 ':'')+fbaEsc(s.modtagerVis)+' · '+fbaDato(s.sidst)+'</span>'+
        '</div></div>';
}
function isupRenderListe(){
    var wrap=document.getElementById("inWrap");if(!wrap||!isupData)return;
    var sager=isupData.sager||[],F=isupFilter;
    var nTil=sager.filter(function(s){return s.tilMig&&s.status==="aaben";}).length,nMine=sager.filter(function(s){return s.mitEget&&s.status==="aaben";}).length;
    var vb=function(m,t,n){return '<button class="isup-v" data-v="'+m+'" style="border:1px solid '+(isupVis===m?'var(--moerk)':'rgba(30,55,76,.16)')+';background:'+(isupVis===m?'var(--moerk)':'#fff')+';color:'+(isupVis===m?'#fff':'var(--moerk)')+';border-radius:999px;cursor:pointer;font-family:var(--font);font-size:.8rem;font-weight:800;padding:6px 14px">'+t+(n?' <span style="background:'+(isupVis===m?'#fff':'#e7004c')+';color:'+(isupVis===m?'var(--moerk)':'#fff')+';border-radius:999px;font-size:.68rem;padding:0 6px">'+n+'</span>':'')+'</button>';};
    var kat=isupData.kat||[],grupper=isupData.grupper||{};
    var opt=function(v,t,sel){return '<option value="'+fbaEsc(v)+'"'+(sel===v?" selected":"")+'>'+fbaEsc(t)+'</option>';};
    var felt='padding:7px 11px;border:1px solid rgba(30,55,76,.16);border-radius:9px;font-family:var(--font);font-size:.82rem;background:#fff';
    var liste=sager.slice();
    if(isupVis==="tilmig")liste=liste.filter(function(s){return s.tilMig;});
    else if(isupVis==="mine")liste=liste.filter(function(s){return s.mitEget;});
    if(F.status)liste=liste.filter(function(s){return s.status===F.status;});
    if(F.gruppe)liste=liste.filter(function(s){return s.modtagerType==="gruppe"&&s.modtager===F.gruppe;});
    if(F.kat)liste=liste.filter(function(s){return s.kategori===F.kat;});
    if(F.prio)liste=liste.filter(function(s){return s.prioritet===F.prio;});
    if(F.q){var q=F.q.toLowerCase();liste=liste.filter(function(s){return (s.emne||"").toLowerCase().indexOf(q)>=0||(s.afVis||"").toLowerCase().indexOf(q)>=0||(s.modtagerVis||"").toLowerCase().indexOf(q)>=0;});}
    var h=inFaneHTML()+
        '<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin:0 0 10px">'+vb("tilmig","📥 Til mig",nTil)+vb("mine","✍️ Sendt af mig",nMine)+vb("alle","Alle",0)+
        '<span style="flex:1"></span>'+(isupData.katRet?'<button id="isupKatBtn" style="'+PROG_KNAP+'">⚙️ Kategorier</button>':'')+'<button id="isupNy" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">➕ Ny sag</button></div>'+
        '<div style="display:flex;gap:8px;flex-wrap:wrap;margin:0 0 12px">'+
        '<input id="isupSoeg" placeholder="🔎 Søg …" value="'+fbaEsc(F.q)+'" style="'+felt+';flex:1;min-width:140px">'+
        '<select id="isupFStatus" style="'+felt+'">'+opt("","Åbne + løste",F.status)+opt("aaben","Kun åbne",F.status)+opt("loest","Kun løste",F.status)+'</select>'+
        '<select id="isupFGrp" style="'+felt+'"><option value="">Alle grupper</option>'+Object.keys(grupper).map(function(k){return opt(k,grupper[k],F.gruppe);}).join("")+'</select>'+
        '<select id="isupFKat" style="'+felt+'"><option value="">Alle kategorier</option>'+kat.map(function(k){return opt(k,k,F.kat);}).join("")+'</select>'+
        '<select id="isupFPrio" style="'+felt+'">'+opt("","Alle prioriteter",F.prio)+opt("haster","🔴 Haster",F.prio)+opt("hoej","🟠 Høj",F.prio)+opt("normal","⚪ Normal",F.prio)+'</select>'+
        '</div>'+
        (isupOpretVis?'<div id="isupOpretBoks"></div>':'')+
        (isupKatVis&&isupData.katRet?'<div id="isupKatBoks"></div>':'')+
        (liste.length?'<div style="display:flex;flex-direction:column;gap:9px">'+liste.map(isupKortHTML).join("")+'</div>':'<div class="fba-kort" style="text-align:center;color:var(--graa);padding:26px">Ingen sager her endnu.</div>');
    wrap.innerHTML=h;inFaneLyt(wrap);
    if(isupOpretVis)isupOpretForm();
    if(isupKatVis&&isupData.katRet)isupKatEditor();
    var kb=document.getElementById("isupKatBtn");if(kb)kb.onclick=function(){isupKatVis=!isupKatVis;isupRenderListe();};
    wrap.querySelectorAll(".isup-v").forEach(function(x){x.onclick=function(){isupVis=x.getAttribute("data-v");isupRenderListe();};});
    var ny=document.getElementById("isupNy");if(ny)ny.onclick=function(){isupOpretVis=!isupOpretVis;isupRenderListe();};
    var sg=document.getElementById("isupSoeg");if(sg){sg.oninput=function(){F.q=sg.value;clearTimeout(window._isupT);window._isupT=setTimeout(isupRenderListe,200);};if(F.q){sg.focus();sg.setSelectionRange(sg.value.length,sg.value.length);}}
    var bind=function(id,key){var el=document.getElementById(id);if(el)el.onchange=function(){F[key]=el.value;isupRenderListe();};};
    bind("isupFStatus","status");bind("isupFGrp","gruppe");bind("isupFKat","kat");bind("isupFPrio","prio");
    wrap.querySelectorAll(".isup-kort").forEach(function(x){x.onclick=function(){isupVisSag(x.getAttribute("data-id"));};});
    isupBkuWire(wrap);
}
function isupOpretForm(){
    var boks=document.getElementById("isupOpretBoks"),sess=fbaSess();if(!boks||!isupData||!sess)return;
    var F='width:100%;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:9px 11px;font-family:var(--font);font-size:.9rem';
    var kat=isupData.kat||[],grupper=isupData.grupper||{},vm=isupData.vismap||{};
    boks.innerHTML='<div class="fba-kort" style="max-width:640px;margin:0 0 14px;display:grid;gap:10px">'+
        '<b style="color:var(--moerk)">➕ Ny intern sag</b>'+
        '<div><label style="font-size:.78rem;color:var(--graa)">Send til</label><select id="isupNyModt" style="'+F+'">'+isupModtagerOpts(vm,grupper,"")+'</select></div>'+
        '<div style="display:flex;gap:10px;flex-wrap:wrap">'+
        '<div style="flex:1;min-width:150px"><label style="font-size:.78rem;color:var(--graa)">Kategori</label><select id="isupNyKat" style="'+F+'"><option value="">— vælg —</option>'+kat.map(function(k){return '<option value="'+fbaEsc(k)+'">'+fbaEsc(k)+'</option>';}).join("")+'</select></div>'+
        '<div style="min-width:130px"><label style="font-size:.78rem;color:var(--graa)">Prioritet</label><select id="isupNyPrio" style="'+F+'"><option value="normal">⚪ Normal</option><option value="hoej">🟠 Høj</option><option value="haster">🔴 Haster</option></select></div>'+
        '<div style="min-width:150px"><label style="font-size:.78rem;color:var(--graa)">Frist (valgfri)</label><input id="isupNyFrist" type="date" style="'+F+'"></div>'+
        '</div>'+
        '<div><label style="font-size:.78rem;color:var(--graa)">Emne</label><input id="isupNyEmne" maxlength="120" placeholder="Kort overskrift på sagen" style="'+F+'"></div>'+
        '<div><label style="font-size:.78rem;color:var(--graa)">Beskrivelse</label><textarea id="isupNyTxt" rows="4" placeholder="Beskriv sagen … et 6-cifret kundenr bliver automatisk klikbart" style="'+F+';resize:vertical"></textarea></div>'+
        '<div id="isupNyFiler"></div>'+
        '<div style="display:flex;gap:8px;align-items:center"><button id="isupNyVedh" type="button" title="Vedhæft billede/fil" style="border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:10px;width:42px;height:42px;font-size:1.05rem;cursor:pointer">📎</button>'+
        '<button id="isupNyGem" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">Opret sag ➤</button>'+
        '<span id="isupNyFejl" style="color:var(--roed);font-size:.85rem"></span><span style="flex:1"></span>'+
        '<button id="isupNyAnnul" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);text-decoration:underline;font-size:.85rem">Annullér</button></div>'+
        '</div>';
    var up=supUploader(document.getElementById("isupNyFiler"),{token:sess.token});
    document.getElementById("isupNyVedh").onclick=function(){up.vaelg();};
    document.getElementById("isupNyAnnul").onclick=function(){isupOpretVis=false;isupRenderListe();};
    document.getElementById("isupNyGem").onclick=async function(){
        var modt=document.getElementById("isupNyModt").value.split(":"),emne=document.getElementById("isupNyEmne").value.trim(),fe=document.getElementById("isupNyFejl");
        if(!emne){fe.textContent="Skriv et emne.";return;}
        var fv=document.getElementById("isupNyFrist").value,frist=fv?Math.floor(new Date(fv+"T09:00:00").getTime()/1000):0;
        this.disabled=true;
        try{var r=await apiKald({a:"isupopret",token:sess.token,modtagerType:modt[0],modtager:modt[1],emne:emne,txt:document.getElementById("isupNyTxt").value,kategori:document.getElementById("isupNyKat").value,prioritet:document.getElementById("isupNyPrio").value,frist:frist,filer:up.filer});
            if(r&&r.ok){isupOpretVis=false;isupData=null;isupSag=r.id;isupStart();}else{this.disabled=false;fe.textContent=(r&&r.fejl)||"Kunne ikke oprette.";}}
        catch(e){this.disabled=false;fe.textContent="Kunne ikke oprette — tjek forbindelsen.";}
    };
}
function isupKatEditor(){
    var boks=document.getElementById("isupKatBoks"),sess=fbaSess();if(!boks||!isupData||!sess)return;
    var kat=(isupData.kat||[]).slice();
    boks.innerHTML='<div class="fba-kort" style="max-width:520px;margin:0 0 14px;display:grid;gap:8px">'+
        '<b style="color:var(--moerk)">⚙️ Rediger kategorier</b>'+
        '<p style="color:var(--graa);font-size:.82rem;margin:0">Én kategori pr. linje. Bruges til at inddele og filtrere interne sager.</p>'+
        '<textarea id="isupKatTxt" rows="7" style="width:100%;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:9px 11px;font-family:var(--font);font-size:.9rem;resize:vertical">'+fbaEsc(kat.join("\n"))+'</textarea>'+
        '<div style="display:flex;gap:8px;align-items:center"><button id="isupKatGem" style="'+PROG_KNAP+';background:var(--moerk);color:#fff;border:0">💾 Gem</button>'+
        '<span id="isupKatFejl" style="color:var(--roed);font-size:.84rem"></span><span style="flex:1"></span>'+
        '<button id="isupKatLuk" style="border:0;background:none;color:var(--graa);cursor:pointer;text-decoration:underline;font-family:var(--font);font-size:.84rem">Luk</button></div>'+
        '</div>';
    document.getElementById("isupKatLuk").onclick=function(){isupKatVis=false;isupRenderListe();};
    document.getElementById("isupKatGem").onclick=async function(){
        var arr=document.getElementById("isupKatTxt").value.split("\n").map(function(s){return s.trim();}).filter(Boolean);
        this.disabled=true;
        try{var r=await apiKald({a:"isupkatgem",token:sess.token,kat:arr});
            if(r&&r.ok){isupData.kat=r.kat;isupKatVis=false;isupRenderListe();}
            else{this.disabled=false;document.getElementById("isupKatFejl").textContent=(r&&r.fejl)||"Kunne ikke gemme";}}
        catch(e){this.disabled=false;document.getElementById("isupKatFejl").textContent="Kunne ikke gemme";}
    };
}
async function isupVisSag(id){
    var wrap=document.getElementById("inWrap"),sess=fbaSess();if(!wrap||!sess)return;
    isupSag=id;
    var j=null;try{j=await apiKald({a:"isuphent",token:sess.token,sid:id});}catch(e){}
    if(!(j&&j.ok)){wrap.innerHTML=inFaneHTML()+'<p style="color:var(--roed)">Kunne ikke hente sagen.</p>';inFaneLyt(wrap);return;}
    var mig=j.mig,pm=ISUP_PRIO[j.prioritet]||ISUP_PRIO.normal,vm=j.vismap||{},grupper=j.grupper||{},kat=j.kat||[];
    var felt='padding:7px 10px;border:1px solid rgba(30,55,76,.16);border-radius:9px;font-family:var(--font);font-size:.82rem;background:#fff';
    var opt=function(v,t,sel){return '<option value="'+fbaEsc(v)+'"'+(sel===v?" selected":"")+'>'+fbaEsc(t)+'</option>';};
    var bobler=(j.beskeder||[]).map(function(b){
        if(b.fra==="system")return '<div style="text-align:center;color:var(--graa);font-size:.77rem;margin:6px 0">'+isupTxt(b.txt)+'</div>';
        var navn=b.afVis||b.af||"Medarbejder";
        return '<div style="display:flex;gap:10px;margin:0 0 14px">'+hfAvatarHTML(b.af,navn)+'<div style="flex:1;min-width:0"><div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(navn)+'</b> · '+fbaDato(b.ts)+'</div>'+(b.txt?'<div style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+isupTxt(b.txt)+'</div>':'')+supFilerHTML(b.filer,false)+'</div></div>';
    }).join("");
    var erTildeltMig=j.tildelt===mig;
    wrap.innerHTML=inFaneHTML()+'<button id="isupRetur" style="'+PROG_KNAP+';margin:0 0 12px">‹ Alle sager</button>'+
        '<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="padding:16px 18px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff">'+
        '<div style="display:flex;align-items:center;gap:9px;flex-wrap:wrap"><span style="display:inline-block;width:12px;height:12px;border-radius:50%;background:'+pm.f+'"></span>'+
        '<b style="font-size:1.12rem;flex:1;min-width:180px">'+isupTxt(j.emne)+'</b>'+(j.status==="loest"?'<span style="background:rgba(255,255,255,.2);border-radius:999px;padding:2px 10px;font-size:.76rem;font-weight:800">✔ Løst</span>':'')+'</div>'+
        '<div style="opacity:.9;font-size:.8rem;margin-top:5px">'+fbaEsc(j.afVis)+' → '+(j.modtagerType==="gruppe"?'👥 ':'')+fbaEsc(j.modtagerVis)+(j.tildeltVis?' · 👤 '+fbaEsc(j.tildeltVis):' · ⏳ i kø')+(j.kategori?' · '+fbaEsc(j.kategori):'')+'</div>'+
        (j.frist?'<div style="margin-top:6px">'+isupFrist(j.frist)+'</div>':'')+'</div>'+
        '<div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;padding:12px 18px;border-bottom:1px solid rgba(30,55,76,.08);background:#fbfdff">'+
        (erTildeltMig?'':'<button id="isupTag" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">✋ Tag sagen</button>')+
        (j.tildelt?'<button id="isupSlip" style="'+PROG_KNAP+'">↩ Giv slip</button>':'')+
        '<button id="isupOverKnap" style="'+PROG_KNAP+'">↪ Overdrag</button>'+
        (j.status==="loest"?'<button id="isupGenaaben" style="'+PROG_KNAP+'">↩ Genåbn</button>':'<button id="isupLoes" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">✔ Markér løst</button>')+
        '<span style="flex:1"></span>'+
        '<select id="isupSetPrio" title="Prioritet" style="'+felt+'">'+opt("normal","⚪ Normal",j.prioritet)+opt("hoej","🟠 Høj",j.prioritet)+opt("haster","🔴 Haster",j.prioritet)+'</select>'+
        '<select id="isupSetKat" title="Kategori" style="'+felt+'"><option value="">— kategori —</option>'+kat.map(function(k){return opt(k,k,j.kategori);}).join("")+'</select>'+
        '</div>'+
        '<div id="isupOverBoks" style="display:none;padding:10px 18px;border-bottom:1px solid rgba(30,55,76,.08);background:#f4f8fb"></div>'+
        '<div style="display:flex;flex-direction:column;padding:16px 18px;background:var(--flade);max-height:48vh;overflow-y:auto">'+bobler+'</div>'+
        '<div style="padding:12px 16px;background:#fff;border-top:1px solid rgba(30,55,76,.08)"><div style="display:flex;gap:10px;align-items:flex-end">'+
        '<button id="isupSvarVedh" type="button" title="Vedhæft" style="border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:10px;width:42px;height:42px;font-size:1.05rem;cursor:pointer;flex:0 0 auto">📎</button>'+
        '<div style="flex:1;position:relative"><textarea id="isupSvarTxt" rows="1" placeholder="Skriv en kommentar … @navn tagger en kollega" style="width:100%;border:1px solid rgba(30,55,76,.16);border-radius:14px;padding:10px 12px;font-family:var(--font);font-size:.9rem;resize:none;line-height:1.35;max-height:120px;overflow-y:auto"></textarea>'+
        '<div id="isupSvarTag" style="position:absolute;z-index:30;left:0;right:0;bottom:calc(100% + 3px);background:#fff;color:var(--moerk);border:1px solid rgba(30,55,76,.2);border-radius:10px;max-height:180px;overflow:auto;display:none;box-shadow:0 8px 24px rgba(0,0,0,.16)"></div></div>'+
        hfEmojiKnap("isupSvarTxt")+'<button id="isupSvarKnap" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">Send ➤</button>'+
        '</div><div id="isupSvarFiler"></div></div></div>';
    inFaneLyt(wrap);hfAvatarWire(wrap);isupBkuWire(wrap);
    document.getElementById("isupRetur").onclick=function(){isupSag=null;isupData=null;isupStart();};
    var kald=async function(a,extra){try{var r=await apiKald(Object.assign({a:a,token:sess.token,sid:id},extra||{}));if(r&&r.ok){isupData=null;isupVisSag(id);}else alert((r&&r.fejl)||"Handlingen fejlede");}catch(e){alert("Handlingen fejlede — tjek forbindelsen.");}};
    var tg=document.getElementById("isupTag");if(tg)tg.onclick=function(){kald("isuptag");};
    var sl=document.getElementById("isupSlip");if(sl)sl.onclick=function(){kald("isupslip");};
    var lo=document.getElementById("isupLoes");if(lo)lo.onclick=function(){kald("isupstatus",{til:"loest"});};
    var ga=document.getElementById("isupGenaaben");if(ga)ga.onclick=function(){kald("isupstatus",{til:"aaben"});};
    document.getElementById("isupSetPrio").onchange=function(){kald("isupret",{prioritet:this.value});};
    document.getElementById("isupSetKat").onchange=function(){kald("isupret",{kategori:this.value});};
    var ob=document.getElementById("isupOverBoks");document.getElementById("isupOverKnap").onclick=function(){
        if(ob.style.display==="none"){ob.style.display="";ob.innerHTML='<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap"><span style="font-size:.82rem;color:var(--graa)">Overdrag til:</span><select id="isupOverSel" style="'+felt+';min-width:200px">'+isupModtagerOpts(vm,grupper,"")+'</select><button id="isupOverGo" style="'+PROG_KNAP+';background:var(--moerk);color:#fff;border:0">Overdrag</button></div>';document.getElementById("isupOverGo").onclick=function(){var v=document.getElementById("isupOverSel").value.split(":");kald("isupoverdrag",{modtagerType:v[0],modtager:v[1]});};}else{ob.style.display="none";}
    };
    var stx=document.getElementById("isupSvarTxt"),svk=document.getElementById("isupSvarVedh"),sknap=document.getElementById("isupSvarKnap");
    var up=supUploader(document.getElementById("isupSvarFiler"),{token:sess.token});
    if(svk)svk.onclick=function(){up.vaelg();};
    stx.addEventListener("input",function(){hfGrow(stx,120);});
    stx.addEventListener("keydown",function(e){if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();sknap.click();}});
    (function(){var tag=document.getElementById("isupSvarTag"),folk=Object.keys(vm).map(function(l){return {l:l,n:vm[l]||l};});var luk=function(){tag.style.display="none";};
        stx.addEventListener("input",function(){var val=stx.value,pos=stx.selectionStart||val.length,foer=val.slice(0,pos),m=foer.match(/@([^\s@]*)$/);if(!m){luk();return;}var q=m[1].toLowerCase(),hit=folk.filter(function(f){return String(f.n).toLowerCase().indexOf(q)>=0||String(f.l).toLowerCase().indexOf(q)>=0;}).slice(0,30);if(!hit.length){luk();return;}tag.innerHTML=hit.map(function(f){return '<div class="isup-nt" data-l="'+fbaEsc(f.l)+'" style="padding:8px 12px;cursor:pointer;font-size:.86rem;border-bottom:1px solid rgba(30,55,76,.06)">👤 '+fbaEsc(f.n)+' <span style="color:var(--graa);font-size:.76rem">@'+fbaEsc(f.l)+'</span></div>';}).join("");tag.style.display="";tag.querySelectorAll(".isup-nt").forEach(function(x){x.onmousedown=function(ev){ev.preventDefault();var login=x.getAttribute("data-l"),start=foer.replace(/@([^\s@]*)$/,"@"+login+" ");stx.value=start+val.slice(pos);var np=start.length;stx.focus();stx.setSelectionRange(np,np);luk();};});});
        stx.addEventListener("blur",function(){setTimeout(luk,180);});
    })();
    sknap.onclick=async function(){var v=(stx.value||"").trim();if(!v&&!up.filer.length)return;sknap.disabled=true;
        try{var r=await apiKald({a:"isupsvar",token:sess.token,sid:id,txt:v,filer:up.filer});if(r&&r.ok){isupData=null;isupVisSag(id);}else{sknap.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}catch(e){sknap.disabled=false;}};
}

/* ===================== 📢 Kanaler — Slack-agtigt forum (v1) ===================== */
var inTilstand="kanaler";try{inTilstand=localStorage.getItem("in_mode")||"kanaler";}catch(e){}
var kanData=null,kanValgt=null,kanInfo=null,kanBeskeder=[],kanCitat=null,kanBillede=null,kanLaestTil=0,
    kanOpretVis=false,kanMedlemVis=false,kanIndstilVis=false,kanTravl=false;
const KAN_TYPE={officiel:"📣",aaben:"🌐",lukket:"🔒"};
function inFaneTal(){   /* {kanaler, traade} — brug den friske liste-data hvor den er hentet, ellers pollerens tal */
    const t=window.hfInternTal||{traade:0,kanaler:0};
    let nK=t.kanaler||0,nT=t.traade||0;
    try{if(kanData&&kanData.kanaler)nK=kanData.kanaler.reduce((s,k)=>s+(k.ulaest||0),0);}catch(e){}
    try{if(inData&&inData.traade)nT=inData.traade.filter(x=>x.ulaest&&!x.arkiv).length;}catch(e){}
    return {kanaler:nK,traade:nT,isup:(window.hfIsupBadge||0)};
}
function inFaneHTML(){
    const tal=inFaneTal();
    const badge=(n)=>n>0?'<span class="in-fane-tal" style="display:inline-block;min-width:17px;text-align:center;background:#e7004c;color:#fff;font-size:.7rem;font-weight:800;border-radius:999px;padding:1px 6px;margin-left:7px">'+n+'</span>':'';
    const b=(m,t,n)=>'<button class="in-fane" data-m="'+m+'" style="'+(inTilstand===m?
            'background:var(--moerk);color:#fff;border:1px solid var(--moerk)':'background:#f4f8fb;border:1px solid rgba(30,55,76,.16);color:var(--moerk)')+
        ';border-radius:999px;cursor:pointer;font-family:var(--font);font-size:.82rem;font-weight:800;padding:6px 16px">'+t+badge(n)+'</button>';
    return '<div id="inFaneRow" style="display:flex;gap:8px;margin:0 0 12px;flex-wrap:wrap">'+b("kanaler","📢 Kanaler",tal.kanaler)+b("traade","✉️ Private tråde",tal.traade)+b("isup","🎫 Intern support",tal.isup)+'</div>';
}
/* live-opdatér fane-tallene når pollerens tal ændrer sig (fx nyt i den fane man IKKE kigger på) */
window.hfInternFaneRefresh=function(){
    const row=document.getElementById("inFaneRow");
    if(!row||location.hash.indexOf("intern")<0)return;
    row.outerHTML=inFaneHTML();
    const w=document.getElementById("inWrap");if(w)inFaneLyt(w);
};
function inFaneLyt(wrap){
    wrap.querySelectorAll(".in-fane").forEach(k=>{k.onclick=()=>{
        inTilstand=k.getAttribute("data-m");try{localStorage.setItem("in_mode",inTilstand);}catch(e){}
        kanValgt=null;inTraad=null;kanCitat=null;kanBillede=null;
        if(inTilstand==="isup"){isupSag=null;isupStart();return;}
        if(inTilstand==="kanaler"){kanStart();return;}
        /* Private tråde: HENT oversigten med det samme (før hang inRenderListe uden data → op til ~18s ventetid) */
        if(inData){inRenderListe();return;}
        const w=document.getElementById("inWrap");
        if(w){w.innerHTML=inFaneHTML()+'<p style="color:var(--graa)">⏳ Henter tråde …</p>';inFaneLyt(w);}
        inHentOversigt().then(j=>{if(j){inData=j;if(j.vismap)ADM_VIS=j.vismap;if(j.titelmap)ADM_TITEL=j.titelmap;}inRenderListe();});
    };});
}
async function kanHentListe(){
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"kanaler",token:sess.token});
        if(j&&j.ok){kanData=j;return j;}}catch(e){}
    return null;
}
async function kanStart(){
    const wrap=document.getElementById("inWrap");if(!wrap)return;
    if(!kanData){wrap.innerHTML=inFaneHTML()+'<p style="color:var(--graa)">⏳ Henter kanaler …</p>';inFaneLyt(wrap);}
    await kanHentListe();
    if(inTilstand!=="kanaler"||location.hash.indexOf("intern")<0)return;
    if(kanValgt)kanVisStroem();else kanRenderListe();
}
function kanRenderListe(){
    const wrap=document.getElementById("inWrap");if(!wrap||!kanData)return;
    /* kanal-rækker + grupper bygges i kanListeFyld — så søgefeltet kan filtrere uden at miste fokus */
    let h=inFaneHTML()+
        '<p class="intro" style="margin:0 0 12px">Kanaler er <b>fælles rum</b> — som medlem ser du alt, der skrives, også det fra før du kom med. Brug ✉️ Private tråde til det fortrolige. <b>Ingen personfølsomme oplysninger eller dokumenter som billeder i kanaler.</b></p>'+
        (kanData.ret?'<div style="margin:0 0 12px"><button id="kanNy" style="'+PROG_KNAP+'">➕ Ny kanal</button></div>':'');
    if(kanOpretVis&&kanData.ret){
        h+='<div id="kanOpret" style="background:#fff;border-radius:14px;padding:14px;box-shadow:var(--skygge);margin:0 0 14px;max-width:520px;display:grid;gap:8px">'+
            '<b style="font-size:.9rem">➕ Ny kanal</b>'+
            '<input id="koNavn" maxlength="60" placeholder="Kanalens navn (fx BPA-faglig)" style="'+PROG_FELT+'">'+
            '<input id="koBesk" maxlength="200" placeholder="Kort beskrivelse — hvad hører til her?" style="'+PROG_FELT+'">'+
            '<select id="koType" style="'+PROG_FELT+'">'+
            '<option value="aaben">🌐 Åben — alle kan se og joine selv</option>'+
            '<option value="lukket">🔒 Lukket — kun synlig for medlemmer</option>'+
            '<option value="officiel">📣 Officiel — alle er automatisk medlem</option></select>'+
            '<select id="koSkriv" style="'+PROG_FELT+'">'+
            '<option value="alle">✍️ Alle medlemmer kan skrive</option>'+
            '<option value="ejere">📢 Kun kanal-ejere kan skrive (opslagstavle)</option></select>'+
            '<div style="display:flex;gap:8px"><button id="koOk" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">Opret kanalen</button>'+
            '<button id="koNej" style="'+PROG_KNAP+'">Annullér</button></div></div>';
    }
    h+='<div class="fba-bar" style="margin:2px 0 10px;gap:6px">'+
        '<input id="kanSoegFelt" type="search" placeholder="🔍 Søg i kanaler …" value="'+fbaEsc(kanSoeg)+'" style="flex:1;min-width:130px;border:2px solid #dbe6ee;border-radius:999px;padding:8px 14px;font-family:var(--font);font-size:.88rem;outline:none">'+
        '<select id="kanListeMedl" style="'+PROG_FELT+';max-width:180px"><option value="">👥 Alle medlemmer</option>'+
        (kanData.admins||[]).map(a=>'<option value="'+fbaEsc(a.login)+'"'+(kanListeMedlem===a.login?' selected':'')+'>'+fbaEsc(a.vis||a.login)+'</option>').join("")+'</select>'+
        '<label class="fba-vis" style="margin-left:0"><input type="checkbox" id="kanListeNv" '+(kanListeNaevnt?"checked":"")+'> Hvor jeg er nævnt</label>'+
        '</div>'+
        '<div id="kanListe"></div>';
    wrap.innerHTML=h;
    inFaneLyt(wrap);
    const ksf=document.getElementById("kanSoegFelt");if(ksf)ksf.oninput=()=>{kanSoeg=ksf.value;kanListeFyld();};
    const kml=document.getElementById("kanListeMedl");if(kml)kml.onchange=()=>{kanListeMedlem=kml.value;kanListeFyld();};
    const knv=document.getElementById("kanListeNv");if(knv)knv.onchange=()=>{kanListeNaevnt=knv.checked;kanListeFyld();};
    kanListeFyld();
    const ny=document.getElementById("kanNy");
    if(ny)ny.onclick=()=>{kanOpretVis=!kanOpretVis;kanRenderListe();};
    const ok=document.getElementById("koOk");
    if(ok)ok.onclick=async function(){
        const navn=document.getElementById("koNavn").value.trim();
        if(!navn){alert("Giv kanalen et navn.");return;}
        this.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"kanopret",token:sess.token,navn:navn,
            besk:document.getElementById("koBesk").value.trim(),
            type:document.getElementById("koType").value,skriv:document.getElementById("koSkriv").value});
            if(r&&r.ok){kanOpretVis=false;await kanHentListe();kanAabn(r.id);}
            else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke oprette");}}
        catch(e){this.disabled=false;}
    };
    const nej=document.getElementById("koNej");
    if(nej)nej.onclick=()=>{kanOpretVis=false;kanRenderListe();};
}
function kanListeFyld(){
    const el=document.getElementById("kanListe");if(!el||!kanData)return;
    const alle=kanData.kanaler||[];
    const q=(kanSoeg||"").trim().toLowerCase();
    const filtAktiv=!!(q||kanListeNaevnt||kanListeMedlem);
    const match=k=>{
        if(q&&String(k.navn||"").toLowerCase().indexOf(q)<0&&String(k.besk||"").toLowerCase().indexOf(q)<0)return false;
        if(kanListeNaevnt&&!k.naevnt)return false;
        if(kanListeMedlem&&!k.alle&&(k.medlemmer||[]).indexOf(kanListeMedlem)<0)return false;
        return true;
    };
    const raek=k=>'<div class="kan-r" data-k="'+fbaEsc(k.id)+'" style="background:#fff;border-radius:14px;padding:12px 16px;box-shadow:var(--skygge);cursor:pointer;display:flex;gap:10px;align-items:center;margin:0 0 8px'+(k.ulaest?';border-left:5px solid var(--roed)':'')+'">'+
        '<span style="font-size:1.15rem">'+(KAN_TYPE[k.type]||"📢")+'</span>'+
        '<span style="flex:1;min-width:0"><b>'+fbaEsc(k.navn)+'</b>'+(k.arkiv?' <span style="color:var(--graa);font-size:.75rem">🗄 arkiveret</span>':'')+
        (k.besk?'<span style="display:block;color:var(--graa);font-size:.78rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">'+fbaEsc(k.besk)+'</span>':'')+'</span>'+
        (k.ulaest?'<span style="background:var(--roed);color:#fff;font-size:.72rem;font-weight:800;border-radius:999px;padding:2px 9px">'+k.ulaest+'</span>':'')+
        '<span style="color:var(--graa);font-size:.76rem;white-space:nowrap">👥 '+k.antal+'</span>'+
        (!k.medlem&&k.type==="aaben"?'<button class="kan-join" data-k="'+fbaEsc(k.id)+'" style="'+PROG_KNAP+'">Bliv medlem</button>':'')+
        '</div>';
    const OVERSKRIFT=t=>'<h4 style="margin:14px 0 8px;font-size:.76rem;letter-spacing:.08em;text-transform:uppercase;color:var(--graa)">'+t+'</h4>';
    const off=alle.filter(k=>k.type==="officiel"&&!k.arkiv&&match(k)),
        mineK=alle.filter(k=>k.type!=="officiel"&&k.medlem&&!k.arkiv&&match(k)),
        gennemse=alle.filter(k=>k.type==="aaben"&&!k.medlem&&!k.arkiv&&match(k)),
        arkiv=alle.filter(k=>k.arkiv&&match(k));
    let h="";
    if(off.length)h+=OVERSKRIFT("📣 Officielle kanaler")+off.map(raek).join("");
    if(mineK.length||!filtAktiv)h+=OVERSKRIFT("⭐ Mine kanaler")+(mineK.length?mineK.map(raek).join(""):'<p style="color:var(--graa);font-size:.85rem">Du er ikke med i nogen kanaler endnu — kig under Gennemse.</p>');
    if(gennemse.length)h+=OVERSKRIFT("🌐 Gennemse — åbne kanaler du kan joine")+gennemse.map(raek).join("");
    if(arkiv.length)h+=OVERSKRIFT("🗄 Arkiverede")+arkiv.map(raek).join("");
    if(filtAktiv&&!off.length&&!mineK.length&&!gennemse.length&&!arkiv.length)h='<p style="color:var(--graa)">Ingen kanaler matcher filteret.</p>';
    el.innerHTML=h;
    el.querySelectorAll(".kan-r").forEach(r=>{r.onclick=e=>{
        if(e.target&&e.target.classList&&e.target.classList.contains("kan-join"))return;
        kanAabn(r.getAttribute("data-k"));
    };});
    el.querySelectorAll(".kan-join").forEach(k=>{k.onclick=async()=>{
        k.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"kanjoin",token:sess.token,kid:k.getAttribute("data-k")});
            if(r&&r.ok){await kanHentListe();kanAabn(k.getAttribute("data-k"));}else k.disabled=false;}
        catch(e){k.disabled=false;}
    };});
}
function kanAabn(kid){
    kanValgt=kid;kanInfo=null;kanBeskeder=[];kanCitat=null;kanBillede=null;kanMedlemVis=false;kanIndstilVis=false;
    beskedMrk={};beskedUdvid.clear();tfOpen=false;
    kanFiltAf="";kanKunNaevnt=false;kanMsgSoeg="";
    kanVisStroem();
}
async function kanPoll(){
    if(!kanValgt)return;
    const sess=fbaSess();if(!sess)return;
    const kid=kanValgt;
    try{const j=await apiKald({a:"kanhent",token:sess.token,kid:kid});
        if(!j||!j.ok||kanValgt!==kid)return;
        const foer=JSON.stringify(kanBeskeder);
        kanInfo=j;kanBeskeder=j.beskeder||[];
        const lFoer=kanLaestTil;if(j.laestTil!==undefined)kanLaestTil=j.laestTil||0;
        const mFoer=JSON.stringify(beskedMrk);beskedMrk=j.mrk||{};
        if(JSON.stringify(kanBeskeder)!==foer||kanLaestTil!==lFoer||JSON.stringify(beskedMrk)!==mFoer)kanLogTegn();
    }catch(e){}
}

/* ===== delt afsender-avatar (profilbillede + initial-fallback) — bruges i kanaler OG private tråde ===== */
function hfAvatarHTML(af,visNavn){
    const nn=String(visNavn||af||"?");
    const init=fbaEsc(nn.substring(0,1).toUpperCase());
    const u=encodeURIComponent(String(af||"").toLowerCase());
    return '<span class="hf-av" data-af="'+fbaEsc(String(af||""))+'" title="Klik for at skrive en besked og tagge '+fbaEsc(nn)+'" style="flex:none;position:relative;width:34px;height:34px;border-radius:50%;overflow:hidden;background:linear-gradient(120deg,var(--blaa),#77c9ec);color:#fff;font-weight:800;display:flex;align-items:center;justify-content:center;font-size:.85rem;cursor:pointer">'+
        '<span>'+init+'</span>'+
        '<img class="hf-av-img" src="mithf-api.php?a=pfoto&u='+u+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0;transition:opacity .15s">'+
        '</span>';
}
function hfAvatarWire(el){
    if(!el)return;
    el.querySelectorAll(".hf-av-img").forEach(function(im){
        var vis=function(){ if(im.naturalWidth>0){ im.style.opacity="1"; } else { im.remove(); } };
        if(im.complete){ vis(); } else { im.onload=function(){ im.style.opacity="1"; }; im.onerror=function(){ im.remove(); }; }
    });
}
/* ===== 😊 emoji-vælger — genbrugelig på skrivefelter ===== */
var HF_EMOJIS=[
    /* ansigter — positive */
    "😀","😃","😄","😁","😆","😅","😂","🤣","🙂","🙃","🫠","😉","😊","😇","🥰","😍","🤩","😘","😗","☺️","😚","😙","🥲","😋","😛","😜","🤪","😝","🤗","🤭","🫢","🤫","🤔","🫡","🤨","😐","😑","😶","🫥","😏","😌","😔","😪","🤤","😴","🥱",
    /* ansigter — negative/udtryk */
    "😷","🤒","🤕","🤢","🤮","🥵","🥶","🥴","😵","😵‍💫","🤯","🤠","🥳","🥸","😎","🤓","🧐","😕","🫤","😟","🙁","☹️","😮","😯","😲","😳","🥺","🥹","😦","😧","😨","😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","😤","😡","😠","🤬",
    /* gestikulation / hænder */
    "👍","👎","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","👇","☝️","✋","🤚","🖐️","🖖","👋","🤝","🫶","🙏","💪","🦾","👏","🙌","🫡","👐","🤲","🫵","✍️","💅","🤳",
    /* hjerter / symboler */
    "❤️","🧡","💛","💚","💙","💜","🤎","🖤","🤍","💔","❤️‍🔥","💕","💞","💓","💗","💖","💘","💝","💯","💢","💫","⭐","🌟","✨","⚡","🔥","🌈","☀️","🎉","🎊","🎈","🎁","🏆","🥇","👑","💎","🔔","💬","💭","👀","👏",
    /* objekter / arbejde */
    "💡","📌","📎","🖇️","📷","📸","📞","☎️","✉️","📨","📝","📋","📅","📆","⏰","⏳","⚠️","✅","❌","❓","❗","➡️","🔗","🔒","🚀","🎯","📈","📊","💰","☕","🍰","🎂","🍕","🍔","🍎","🌸","🌷","🌻","🐾","🐶","🐱"
];
function hfIndsaetTegn(input,tegn){
    if(!input)return;input.focus();
    try{var s=input.selectionStart,e=input.selectionEnd,v=input.value||"";input.value=v.slice(0,s)+tegn+v.slice(e);input.selectionStart=input.selectionEnd=s+tegn.length;}
    catch(err){input.value=(input.value||"")+tegn;}
    input.focus();
}
function hfEmojiVis(input,anchor){
    var eks=document.getElementById("hfEmojiPanel");if(eks){eks.remove();if(eks.__for===anchor)return;}
    var p=document.createElement("div");p.id="hfEmojiPanel";p.__for=anchor;
    p.style.cssText="position:absolute;z-index:9997;background:#fff;border:1px solid rgba(30,55,76,.15);border-radius:14px;box-shadow:0 14px 44px rgba(0,0,0,.22);padding:8px;display:grid;grid-template-columns:repeat(8,30px);gap:1px;max-height:224px;overflow-y:auto";
    p.innerHTML=HF_EMOJIS.map(function(e){return '<button type="button" class="hf-emo" style="border:0;background:none;cursor:pointer;font-size:1.2rem;padding:2px;border-radius:8px;line-height:1.1">'+e+'</button>';}).join("");
    document.body.appendChild(p);
    var r=(anchor||input).getBoundingClientRect();
    var venstre=Math.max(8,Math.min(r.left,window.innerWidth-p.offsetWidth-8));
    var top=r.top-p.offsetHeight-6;if(top<6)top=r.bottom+6;
    p.style.left=(venstre+window.scrollX)+"px";p.style.top=(top+window.scrollY)+"px";
    p.querySelectorAll(".hf-emo").forEach(function(b){
        b.onmouseenter=function(){b.style.background="#eef4f8";};b.onmouseleave=function(){b.style.background="none";};
        b.onclick=function(){hfIndsaetTegn(input,b.textContent);p.remove();};
    });
    setTimeout(function(){document.addEventListener("click",function luk(ev){var pp=document.getElementById("hfEmojiPanel");if(pp&&!pp.contains(ev.target)&&ev.target!==anchor){pp.remove();document.removeEventListener("click",luk);}});},30);
}
function hfEmojiKnap(inputId){   /* returnerer HTML for en 😊-knap der åbner vælgeren på inputId */
    return '<button type="button" class="hf-emoji-btn" data-input="'+inputId+'" title="Indsæt emoji" style="border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:10px;cursor:pointer;font-size:1.05rem;line-height:1;padding:6px 9px;flex:none">😊</button>';
}
if(!window.__hfEmojiInit){window.__hfEmojiInit=1;
    document.addEventListener("click",function(e){
        var b=e.target&&e.target.closest&&e.target.closest(".hf-emoji-btn");if(!b)return;
        e.preventDefault();e.stopPropagation();
        hfEmojiVis(document.getElementById(b.getAttribute("data-input")),b);
    });
}
/* klik på en afsender-avatar → indsæt @tag i skrivefeltet + fokus (kanaler + tråde) */
function hfTagIfelt(inputId,af){
    var felt=document.getElementById(inputId);if(!felt||!af)return;
    var v=felt.value||"";
    if(v.toLowerCase().indexOf("@"+String(af).toLowerCase())<0){
        felt.value=(v && !/\s$/.test(v)?v+" ":v)+"@"+af+" ";
    }
    felt.focus();
    try{felt.selectionStart=felt.selectionEnd=felt.value.length;}catch(e){}
}
function hfAvatarTagWire(log,inputId){
    if(!log)return;
    log.querySelectorAll(".hf-av").forEach(function(a){
        a.onclick=function(e){e.preventDefault();e.stopPropagation();hfTagIfelt(inputId,a.getAttribute("data-af"));};
    });
}

/* ===== redigér opslag (kanaler + private tråde) — rettighedsstyret ===== */
/* rettigheder kommer fra det AKTIVE kanal-/tråd-serversvar (maaRed=redigerbesked, maaMod=moderation) —
   sættes i kanLogTegn/inLogTegn før render, så det virker uanset om live-chat-konteksten (lcaData) er hentet */
let beskRedEgne=false,beskRedMod=false;
function beskMaaRedigere(b){
    if(!b||!b.bid)return false;   /* gamle beskeder uden id kan ikke redigeres */
    const mig=b.af===(fbaSess()||{}).u;
    return (mig&&beskRedEgne)||beskRedMod;   /* egne m. rettighed, andres kræver moderation */
}

function beskRetWire(log,kilde){
    log.querySelectorAll('.besk-ret[data-kilde="'+kilde+'"]').forEach(function(btn){
        btn.onclick=function(){beskRetStart(btn,kilde);};
    });
}

function beskRetStart(btn,kilde){
    const bid=btn.getAttribute("data-b");
    const arr=kilde==="kan"?kanBeskeder:inBeskeder;
    const b=(arr||[]).find(x=>x.bid===bid);
    if(!b)return;
    const kol=btn.parentElement.parentElement;   /* besked-kolonnen (flex:1) */
    const txtEl=kol.querySelector(".besk-txt");
    if(txtEl&&txtEl.dataset.redigerer)return;
    const raa=b.txt||"";
    const box=document.createElement("div");box.style.marginTop="4px";
    const ta=document.createElement("textarea");
    ta.value=raa;ta.maxLength=(kilde==="kan"?3000:2000);
    ta.style.cssText="width:100%;box-sizing:border-box;border:2px solid var(--blaa);border-radius:10px;padding:8px 10px;font-family:var(--font);font-size:.92rem;resize:vertical;min-height:58px;outline:none";
    const rk=document.createElement("div");rk.style.cssText="display:flex;gap:8px;margin-top:6px;align-items:center";
    rk.innerHTML='<button class="brGem" style="border:0;background:var(--blaa);color:#fff;border-radius:8px;padding:6px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">Gem</button><button class="brAnn" style="border:0;background:rgba(30,55,76,.1);border-radius:8px;padding:6px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">Annullér</button><span style="font-size:.72rem;color:var(--graa)">Ændringen markeres «redigeret»</span>';
    box.appendChild(ta);box.appendChild(rk);
    if(txtEl){txtEl.dataset.redigerer="1";txtEl.style.display="none";txtEl.parentNode.insertBefore(box,txtEl.nextSibling);}
    else{kol.appendChild(box);}
    ta.focus();try{ta.selectionStart=ta.selectionEnd=ta.value.length;}catch(e){}
    function luk(){box.remove();if(txtEl){txtEl.style.display="";delete txtEl.dataset.redigerer;}}
    rk.querySelector(".brAnn").onclick=luk;
    rk.querySelector(".brGem").onclick=async function(){
        const ny=ta.value.trim();
        if(!ny){alert("Teksten må ikke være tom.");return;}
        if(ny===raa){luk();return;}
        const gem=this;gem.disabled=true;gem.textContent="Gemmer …";
        const sess=fbaSess();
        const p=(kilde==="kan")?{a:"kanbeskret",token:sess.token,kid:kanValgt,bid:bid,txt:ny}
            :{a:"inret",token:sess.token,tid:inTraad,bid:bid,txt:ny};
        try{const r=await apiKald(p);
            if(r&&r.ok){b.txt=ny;b.redigeret=r.redigeret||Math.floor(Date.now()/1000);luk();if(kilde==="kan")kanLogTegn();else inLogTegn();}
            else{alert((r&&r.fejl)||"Kunne ikke gemme ændringen");gem.disabled=false;gem.textContent="Gem";}
        }catch(e){alert("Kunne ikke gemme — tjek forbindelsen.");gem.disabled=false;gem.textContent="Gem";}
    };
}
function kanBeskedHTML(b){
    const mig=b.af===(fbaSess()||{}).u;
    const _linje=mrkLinje("kan",b,(b.afVis||b.af),(b.txt||"📷 billede"));if(_linje)return _linje;
    return '<div style="display:flex;gap:10px;margin:0 0 14px">'+
        hfAvatarHTML(b.af,(b.afVis||b.af))+
        '<div style="flex:1;min-width:0"><div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(b.afVis||b.af)+'</b>'+(typeof visTitelHTML==="function"?visTitelHTML(b.af):'')+' · '+fbaDato(b.ts)+(b.redigeret?' · <span style="font-style:italic" title="Redigeret '+fbaDato(b.redigeret)+'">redigeret</span>':'')+'</div>'+
        (b.citat?'<div style="border-left:3px solid rgba(30,55,76,.25);background:rgba(30,55,76,.05);border-radius:0 8px 8px 0;padding:5px 10px;margin:4px 0;font-size:.8rem;color:var(--graa)"><b>'+fbaEsc(b.citat.af)+':</b> '+fbaEsc(b.citat.txt)+'</div>':'')+
        (b.txt?'<div class="besk-txt" style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+inTagHTML(b.txt)+'</div>':'')+
        (b.billede?'<img class="hf-lb" data-url="mithf-api.php?a=kanbillede&f='+fbaEsc(b.billede)+'" src="mithf-api.php?a=kanbillede&f='+fbaEsc(b.billede)+'" alt="billede" loading="lazy" style="max-width:min(340px,100%);border-radius:12px;margin-top:6px;box-shadow:var(--skygge);cursor:zoom-in">':'')+
        (b.filer&&b.filer.length?supFilerHTML(b.filer,false):'')+
        (b.poll?kanPollHTML(b):'')+
        '<div style="display:flex;gap:6px;margin-top:5px">'+
        '<button class="kan-reak" data-b="'+fbaEsc(b.bid)+'" data-r="op" style="border:1px solid rgba(30,55,76,'+(b.min==="op"?'.6':'.14')+');background:'+(b.min==="op"?'#eaf6fd':'#fff')+';border-radius:999px;cursor:pointer;font-size:.75rem;padding:2px 9px;font-family:var(--font)">👍'+(b.op?' '+b.op:'')+'</button>'+
        '<button class="kan-reak" data-b="'+fbaEsc(b.bid)+'" data-r="hjerte" style="border:1px solid rgba(30,55,76,'+(b.min==="hjerte"?'.6':'.14')+');background:'+(b.min==="hjerte"?'#fdeef3':'#fff')+';border-radius:999px;cursor:pointer;font-size:.75rem;padding:2px 9px;font-family:var(--font)">❤️'+(b.hjerte?' '+b.hjerte:'')+'</button>'+
        (kanInfo&&kanInfo.maaSkrive?'<button class="kan-svar" data-b="'+fbaEsc(b.bid)+'" style="border:0;background:none;cursor:pointer;font-size:.75rem;color:var(--graa);font-family:var(--font);font-weight:700">↩ Svar</button>':'')+
        (beskMaaRedigere(b)?'<button class="besk-ret" data-kilde="kan" data-b="'+fbaEsc(b.bid)+'" style="border:0;background:none;cursor:pointer;font-size:.75rem;color:var(--graa);font-family:var(--font);font-weight:700">✏️ Redigér</button>':'')+
        mrkKnapper("kan",b)+
        '</div></div></div>';
}
function kanPollHTML(b){   /* 📊 afstemning: søjler m. stemmetal — klik stemmer (klik igen fjerner) */
    const tal=b.pollTal||[],ialt=tal.reduce((s,x)=>s+x,0);
    return '<div style="border:1px solid rgba(30,55,76,.14);border-radius:12px;padding:10px 12px;margin-top:6px;max-width:420px">'+
        '<b style="font-size:.9rem">📊 '+fbaEsc(b.poll.spm)+'</b>'+
        b.poll.valg.map((v,ix)=>{
            const n=tal[ix]||0,pct=ialt?Math.round(n*100/ialt):0,min=b.pollMin===ix;
            return '<div class="kan-stem" data-b="'+fbaEsc(b.bid)+'" data-v="'+ix+'" style="cursor:pointer;margin-top:7px" title="'+(min?"Klik for at fjerne din stemme":"Klik for at stemme")+'">'+
                '<div style="display:flex;justify-content:space-between;font-size:.82rem"><span>'+(min?"✅ ":"")+fbaEsc(v)+'</span><span style="color:var(--graa)">'+n+(ialt?' · '+pct+'%':'')+'</span></div>'+
                '<div style="height:7px;border-radius:99px;background:rgba(30,55,76,.08);margin-top:3px"><div style="height:7px;border-radius:99px;width:'+pct+'%;background:'+(min?'var(--groen)':'var(--blaa)')+'"></div></div></div>';
        }).join("")+
        '<div style="color:var(--graa);font-size:.72rem;margin-top:7px">'+ialt+' stemme'+(ialt===1?"":"r")+'</div></div>';
}
/* ===== «læst til her»-fold (delt af kanaler + private tråde) ===== */
function tfWrap(b,beskedHTML){
    const ts=b.ts||0;
    return '<div class="tf-m" data-lts="'+ts+'">'+beskedHTML(b)+
        '<button class="tf-laest" data-lts="'+ts+'" title="Fold alt til og med denne besked sammen som læst">✓ læst til her</button></div>';
}
function tfRenderHTML(beskeder,laestTil,beskedHTML){
    const read=[],ny=[];
    (beskeder||[]).forEach(b=>{((b.ts||0)<=laestTil?read:ny).push(b);});
    let h="";
    if(read.length){
        h+='<details class="tf-fold"'+(tfOpen?' open':'')+'><summary class="tf-sum">📁 '+read.length+' læst'+(read.length===1?' besked':'e beskeder')+' <span class="tf-vis">— vis / skjul</span></summary>'+
            '<div class="tf-read">'+read.map(b=>tfWrap(b,beskedHTML)).join("")+'</div></details>';
        if(ny.length)h+='<div class="tf-ny"><span>Nyt herunder</span></div>';
    }
    h+=ny.map(b=>tfWrap(b,beskedHTML)).join("");
    return h;
}
function tfWire(log,onSaet){
    log.querySelectorAll(".tf-laest").forEach(b=>{b.onclick=(e)=>{e.stopPropagation();e.preventDefault();onSaet(parseInt(b.getAttribute("data-lts"),10)||0);};});
    const det=log.querySelector("details.tf-fold");
    if(det)det.addEventListener("toggle",()=>{tfOpen=det.open;});
}
function tfHopUlaest(logId){   /* hop til «Nyt herunder»-stregen (første ulæste), ellers bunden */
    const log=document.getElementById(logId);if(!log)return;
    const ny=log.querySelector(".tf-ny");
    if(ny)ny.scrollIntoView({behavior:"smooth",block:"center"});
    else log.scrollTop=log.scrollHeight;
}
/* ===== enkelt-post «læst»/«behandlet»-markering (folder DEN post) ===== */
function mrkKey(kilde,b){return kilde==="kan"?("k:"+b.bid):("t:"+(b.ts||0)+":"+(b.af||""));}
function mrkLinje(kilde,b,sender,preview){
    const key=mrkKey(kilde,b),m=beskedMrk[key]||"";
    if(!m||beskedUdvid.has(key))return null;
    const groen=m==="behandlet";
    return '<div class="mrk-linje" data-key="'+fbaEsc(key)+'" style="display:flex;align-items:center;gap:8px;background:'+(groen?'rgba(147,213,0,.13)':'rgba(30,55,76,.05)')+';border:1px solid '+(groen?'rgba(147,213,0,.3)':'rgba(30,55,76,.08)')+';border-radius:10px;padding:6px 12px;margin:0 0 8px;cursor:pointer;font-size:.8rem">'+
        '<b style="color:'+(groen?'#41630a':'var(--graa)')+';white-space:nowrap">'+(groen?'✔ behandlet':'✓ læst')+'</b>'+
        '<span style="color:var(--graa);font-weight:700;white-space:nowrap">'+fbaEsc(sender)+'</span>'+
        '<span style="color:var(--graa);flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">'+fbaEsc(preview)+'</span>'+
        '<span style="color:var(--blaa);font-weight:700;white-space:nowrap">vis ▾</span></div>';
}
function mrkKnapper(kilde,b){
    const key=mrkKey(kilde,b),m=beskedMrk[key]||"";
    const knap=(mm,lbl,akt)=>'<button class="mrk-set" data-key="'+fbaEsc(key)+'" data-m="'+mm+'" style="border:1px solid rgba(30,55,76,.16);background:'+(m===mm?akt:'#fff')+';color:'+(m===mm?(mm==="behandlet"?"#41630a":"var(--moerk)"):'var(--graa)')+';border-radius:999px;font-family:var(--font);font-size:.68rem;font-weight:800;padding:2px 9px;cursor:pointer">'+lbl+'</button>';
    let s='<span class="mrk-rk" style="display:inline-flex;gap:6px;align-items:center;margin-left:4px">'+knap("laest","✓ læst","#eef3f7")+knap("behandlet","✔ behandlet","#eaffce");
    if(m&&beskedUdvid.has(key))s+='<button class="mrk-fold" data-key="'+fbaEsc(key)+'" style="border:0;background:none;color:var(--graa);font-size:.68rem;font-weight:800;cursor:pointer;font-family:var(--font)">fold ▲</button>';
    return s+'</span>';
}
function mrkWire(el){
    el.querySelectorAll(".mrk-linje").forEach(x=>{x.onclick=()=>{beskedUdvid.add(x.getAttribute("data-key"));mrkRefresh();};});
    el.querySelectorAll(".mrk-set").forEach(b=>{b.onclick=(e)=>{e.stopPropagation();const key=b.getAttribute("data-key"),mm=b.getAttribute("data-m");mrkSaet(key,(beskedMrk[key]===mm)?"":mm);};});
    el.querySelectorAll(".mrk-fold").forEach(b=>{b.onclick=(e)=>{e.stopPropagation();beskedUdvid.delete(b.getAttribute("data-key"));mrkRefresh();};});
}
function mrkSaet(key,mrk){
    const sess=fbaSess();if(!sess)return;
    if(mrk)beskedMrk[key]=mrk;else delete beskedMrk[key];
    beskedUdvid.delete(key);
    mrkRefresh();
    apiKald({a:"beskedmrk",token:sess.token,key:key,mrk:mrk}).catch(()=>{});
}
function mrkRefresh(){ if(kanValgt)kanLogTegn(); if(inTraad)inLogTegn(); }
async function kanSaetLaest(ts){
    const sess=fbaSess();if(!sess||!kanValgt)return;
    kanLaestTil=ts;tfOpen=false;kanLogTegn();
    try{await apiKald({a:"kanlaest",token:sess.token,kid:kanValgt,ts:ts});}catch(e){}
}
function kanFiltFyld(){   /* #3/#4: fyld afsender-dropdown fra kanalens beskeder */
    const sel=document.getElementById("kanFiltAfSel");if(!sel)return;
    const seen={},opts=[];
    (kanBeskeder||[]).forEach(b=>{if(b.af&&!seen[b.af]){seen[b.af]=1;opts.push(b.af);}});
    opts.sort((a,b)=>String(visA(a)).localeCompare(String(visA(b)),'da'));
    sel.innerHTML='<option value="">👥 Alle afsendere</option>'+opts.map(l=>'<option value="'+fbaEsc(l)+'"'+(kanFiltAf===l?' selected':'')+'>'+fbaEsc(visA(l))+'</option>').join("");
}
function kanLogTegn(){
    const log=document.getElementById("kanLog");if(!log)return;
    beskRedEgne=!!(kanInfo&&kanInfo.maaRed);beskRedMod=!!(kanInfo&&kanInfo.maaMod);
    const iBunden=log.scrollHeight-log.scrollTop-log.clientHeight<80||!log.dataset.tegnet;
    let vis=kanBeskeder;
    const q=(kanMsgSoeg||"").trim().toLowerCase();
    if(kanFiltAf)vis=vis.filter(b=>b.af===kanFiltAf);
    if(q)vis=vis.filter(b=>String(b.txt||"").toLowerCase().indexOf(q)>=0);
    if(kanKunNaevnt){const mig=(((fbaSess()||{}).u)||"").toLowerCase();vis=vis.filter(b=>String(b.txt||"").toLowerCase().indexOf("@"+mig)>=0);}
    const filtreret=!!(kanFiltAf||q||kanKunNaevnt);
    log.innerHTML=vis.length?tfRenderHTML(vis,filtreret?0:(kanLaestTil||0),kanBeskedHTML)
        :(filtreret?'<p style="color:var(--graa)">Ingen beskeder matcher filteret.</p>':'<p style="color:var(--graa)">Ingen beskeder endnu'+(kanInfo&&kanInfo.maaSkrive?' — skriv den første 👇':'')+'</p>');
    log.dataset.tegnet="1";
    if(vis.length){tfWire(log,kanSaetLaest);mrkWire(log);hfAvatarWire(log);hfAvatarTagWire(log,"kanTxt");beskRetWire(log,"kan");}
    if(iBunden)log.scrollTop=log.scrollHeight;
    log.querySelectorAll(".kan-reak").forEach(k=>{k.onclick=async()=>{
        const sess=fbaSess();
        try{const r=await apiKald({a:"kanreak",token:sess.token,bid:k.getAttribute("data-b"),reak:k.getAttribute("data-r")});
            if(r&&r.ok)kanPoll();}catch(e){}
    };});
    log.querySelectorAll(".kan-svar").forEach(k=>{k.onclick=()=>{
        const b=kanBeskeder.find(x=>x.bid===k.getAttribute("data-b"));
        if(!b)return;
        kanCitat={bid:b.bid,af:b.afVis||b.af,txt:(b.txt||"📷 billede").substring(0,140)};
        kanCitatTegn();
        const felt=document.getElementById("kanTxt");if(felt)felt.focus();
    };});
    log.querySelectorAll(".kan-stem").forEach(k=>{k.onclick=async()=>{
        const sess=fbaSess();
        try{const r=await apiKald({a:"kanstem",token:sess.token,bid:k.getAttribute("data-b"),valg:parseInt(k.getAttribute("data-v"),10)});
            if(r&&r.ok)kanPoll();else alert((r&&r.fejl)||"Kunne ikke stemme");}catch(e){}
    };});
}
function kanCitatTegn(){
    const el=document.getElementById("kanCitatVis");if(!el)return;
    el.innerHTML=kanCitat?'<div style="display:flex;gap:8px;align-items:center;background:rgba(30,55,76,.06);border-radius:9px;padding:5px 10px;font-size:.78rem;color:var(--graa)">↩ Svar til <b>'+fbaEsc(kanCitat.af)+'</b>: '+fbaEsc(kanCitat.txt.substring(0,60))+'…<button id="kanCitatVaek" style="border:0;background:none;cursor:pointer;margin-left:auto">✕</button></div>':'';
    const v=document.getElementById("kanCitatVaek");
    if(v)v.onclick=()=>{kanCitat=null;kanCitatTegn();};
}
function kanBilledeTegn(){
    const el=document.getElementById("kanBilledeVis");if(!el)return;
    el.innerHTML=kanBillede?'<div style="display:flex;gap:8px;align-items:center;font-size:.78rem;color:var(--graa)"><img src="mithf-api.php?a=kanbillede&f='+fbaEsc(kanBillede)+'" style="height:44px;border-radius:8px"> billede vedhæftet <button id="kanBilledeVaek" style="border:0;background:none;cursor:pointer">✕</button></div>'
        :(kanTravl?'<span style="font-size:.78rem;color:var(--graa)">⏳ uploader billede …</span>':'');
    const v=document.getElementById("kanBilledeVaek");
    if(v)v.onclick=()=>{kanBillede=null;kanBilledeTegn();};
}
async function kanVisStroem(){
    const wrap=document.getElementById("inWrap");if(!wrap)return;
    wrap.innerHTML=inFaneHTML()+'<p style="color:var(--graa)">⏳ Åbner kanalen …</p>';
    inFaneLyt(wrap);
    await kanPoll();
    if(!kanInfo||kanValgt===null)return;
    const k=kanInfo;
    let h=inFaneHTML()+
        '<div style="background:#fff;border-radius:16px;box-shadow:var(--skygge);padding:14px 16px;margin:0 0 12px">'+
        '<div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">'+
        '<button id="kanTilbage" style="'+PROG_KNAP+'">‹ Kanaler</button>'+
        '<b style="font-size:1.05rem">'+(KAN_TYPE[k.type]||"📢")+' '+fbaEsc(k.navn)+'</b>'+
        (k.arkiv?'<span style="color:var(--graa);font-size:.78rem">🗄 arkiveret</span>':'')+
        '<span style="flex:1"></span>'+
        '<button id="kanMedlKnap" style="'+PROG_KNAP+'">👥 '+k.medlemmer.length+(k.type==="officiel"?"+ alle":"")+'</button>'+
        (k.ejer?'<button id="kanIndstilKnap" style="'+PROG_KNAP+'">⚙️</button>':'')+
        (!k.medlem&&k.type==="aaben"&&!k.arkiv?'<button id="kanJoinKnap" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">Bliv medlem</button>':'')+
        (k.medlem&&k.type!=="officiel"?'<button id="kanForladKnap" style="'+PROG_KNAP+';color:var(--roed)">Forlad</button>':'')+
        '</div>'+
        (k.besk?'<p style="color:var(--graa);font-size:.82rem;margin:6px 0 0">'+fbaEsc(k.besk)+(k.skriv==="ejere"?' · 📢 kun kanal-ejere skriver':'')+'</p>':(k.skriv==="ejere"?'<p style="color:var(--graa);font-size:.82rem;margin:6px 0 0">📢 kun kanal-ejere skriver</p>':''))+
        '<div id="kanMedl" style="display:none;margin-top:10px"></div>'+
        '<div id="kanIndstil" style="display:none;margin-top:10px"></div>'+
        '</div>'+
        '<div class="fba-bar" style="margin:0 0 10px;gap:6px">'+
        '<input id="kanMsgSoeg" type="search" placeholder="🔍 Søg i beskederne …" value="'+fbaEsc(kanMsgSoeg)+'" style="flex:1;min-width:130px;border:2px solid #dbe6ee;border-radius:999px;padding:7px 13px;font-family:var(--font);font-size:.85rem;outline:none">'+
        '<select id="kanFiltAfSel" style="'+PROG_FELT+';max-width:180px"><option value="">👥 Alle afsendere</option></select>'+
        '<label class="fba-vis" style="margin-left:0"><input type="checkbox" id="kanKunNv" '+(kanKunNaevnt?"checked":"")+'> Kun hvor jeg er nævnt</label>'+
        '</div>'+
        '<div id="kanLog" style="background:#fff;border-radius:16px;box-shadow:var(--skygge);padding:16px;height:52vh;overflow-y:auto"></div>'+
        (k.maaSkrive?'<div style="background:#fff;border-radius:16px;box-shadow:var(--skygge);padding:10px 12px;margin-top:10px">'+
            '<div id="kanCitatVis"></div><div id="kanBilledeVis"></div><div id="kanVedhVis"></div>'+
            '<div style="display:flex;gap:8px;align-items:flex-end;margin-top:4px">'+
            '<textarea id="kanTxt" rows="2" maxlength="3000" placeholder="Skriv i kanalen … @navn giver besked" style="flex:1;border:1px solid rgba(30,55,76,.16);border-radius:11px;padding:9px 12px;font-family:var(--font);font-size:.9rem;resize:vertical"></textarea>'+
            '<input id="kanFil" type="file" accept="image/*" style="display:none">'+
            '<button id="kanFoto" title="Vedhæft billede — på telefonen kan du tage et nyt" style="'+PROG_KNAP+';font-size:1rem">📷</button>'+
            '<button id="kanVedh" title="Vedhæft fil (Word, Excel, PDF, billede — maks 8 MB)" style="'+PROG_KNAP+';font-size:1rem">📎</button>'+
            '<button id="kanPollKnap" title="Lav en afstemning" style="'+PROG_KNAP+';font-size:1rem">📊</button>'+
            hfEmojiKnap("kanTxt")+
            '<button id="kanSend" style="'+PROG_KNAP+';background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff;border:0">Send ➤</button>'+
            '</div>'+
            '<div id="kanForslag" style="display:none;flex-wrap:wrap;gap:6px;margin-top:6px"></div>'+
            '<div id="kanPollForm" style="display:none;margin-top:8px;border-top:1px solid rgba(30,55,76,.1);padding-top:8px">'+
            '<input id="kpSpm" maxlength="200" placeholder="📊 Spørgsmål (fx: Fredagsmorgenmad?)" style="'+PROG_FELT+';margin:0 0 6px">'+
            [0,1,2,3].map(i=>'<input class="kp-valg" maxlength="80" placeholder="Svarmulighed '+(i+1)+(i>1?" (valgfri)":"")+'" style="'+PROG_FELT+';margin:0 0 6px">').join("")+
            '<button id="kpSend" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">Send afstemningen</button>'+
            '</div></div>'
            :(k.medlem?'':'<p style="color:var(--graa);font-size:.85rem;margin-top:10px">Du læser med udefra — bliv medlem for at skrive.</p>'));
    wrap.innerHTML=h;
    inFaneLyt(wrap);
    kanLogTegn();kanCitatTegn();kanBilledeTegn();kanFiltFyld();
    (function(){
        const ms=document.getElementById("kanMsgSoeg");if(ms)ms.oninput=()=>{kanMsgSoeg=ms.value;kanLogTegn();};
        const fa=document.getElementById("kanFiltAfSel");if(fa)fa.onchange=()=>{kanFiltAf=fa.value;kanLogTegn();};
        const kn=document.getElementById("kanKunNv");if(kn)kn.onchange=()=>{kanKunNaevnt=kn.checked;kanLogTegn();};
    })();
    document.getElementById("kanTilbage").onclick=()=>{kanValgt=null;kanStart();};
    const jn=document.getElementById("kanJoinKnap");
    if(jn)jn.onclick=async()=>{const sess=fbaSess();
        try{const r=await apiKald({a:"kanjoin",token:sess.token,kid:kanValgt});if(r&&r.ok)kanVisStroem();}catch(e){}};
    const fl=document.getElementById("kanForladKnap");
    if(fl)fl.onclick=async()=>{
        if(!confirm("Forlad kanalen?"+(k.type==="lukket"?" Du skal inviteres ind igen.":"")))return;
        const sess=fbaSess();
        try{const r=await apiKald({a:"kanforlad",token:sess.token,kid:kanValgt});
            if(r&&r.ok){kanValgt=null;kanStart();}}catch(e){}};
    const mk=document.getElementById("kanMedlKnap");
    if(mk)mk.onclick=()=>{kanMedlemVis=!kanMedlemVis;kanIndstilVis=false;kanPanelTegn();};
    const ik=document.getElementById("kanIndstilKnap");
    if(ik)ik.onclick=()=>{kanIndstilVis=!kanIndstilVis;kanMedlemVis=false;kanPanelTegn();};
    kanPanelTegn();
    (function(){var _kt=document.getElementById("kanTxt"),_kf=document.getElementById("kanForslag");   /* @navn + @rettighedsgrupper + @tag-grupper i kanaler (serveren notificerer allerede) */
        if(_kt&&_kf)hfTagGrpHent().then(function(){var extra=(hfTagGrp||[]).filter(function(t){return t.maaBruge;}).map(function(t){return t.handle;});atForslag(_kt,_kf,GRP_TAG_LISTE.concat((kanData.admins||[]).map(function(a){return typeof a==="string"?a:a.login;})).concat(extra));});})();
    const foto=document.getElementById("kanFoto"),fil=document.getElementById("kanFil");
    const kanUp=supUploader(document.getElementById("kanVedhVis"),{token:(fbaSess()||{}).token});
    const kanVedhBtn=document.getElementById("kanVedh");if(kanVedhBtn)kanVedhBtn.onclick=()=>kanUp.vaelg();
    if(foto&&fil){
        foto.onclick=()=>fil.click();
        fil.onchange=()=>{
            const f=fil.files&&fil.files[0];if(!f)return;
            if(f.size>5*1024*1024){alert("Billedet er for stort — maks 5 MB.");fil.value="";return;}
            kanTravl=true;kanBilledeTegn();
            const l=new FileReader();
            l.onload=async()=>{
                const sess=fbaSess();
                try{const r=await apiKald({a:"kanupload",token:sess.token,billede:l.result});
                    kanTravl=false;
                    if(r&&r.ok)kanBillede=r.navn;else alert((r&&r.fejl)||"Kunne ikke uploade billedet.");
                    kanBilledeTegn();}
                catch(e){kanTravl=false;kanBilledeTegn();alert("Kunne ikke uploade — tjek forbindelsen.");}
            };
            l.readAsDataURL(f);
            fil.value="";
        };
    }
    const pk=document.getElementById("kanPollKnap"),pf=document.getElementById("kanPollForm");
    if(pk&&pf)pk.onclick=()=>{pf.style.display=pf.style.display==="none"?"":"none";};
    const ps=document.getElementById("kpSend");
    if(ps)ps.onclick=async function(){
        const spm=document.getElementById("kpSpm").value.trim();
        const valg=[];document.querySelectorAll("#kanPollForm .kp-valg").forEach(i=>{const v=i.value.trim();if(v)valg.push(v);});
        if(!spm||valg.length<2){alert("Skriv et spørgsmål og mindst 2 svarmuligheder.");return;}
        this.disabled=true;const sess=fbaSess();
        try{const r=await apiKald({a:"kansend",token:sess.token,kid:kanValgt,txt:"",billede:"",citat:"",poll:{spm:spm,valg:valg}});
            if(r&&r.ok){pf.style.display="none";document.getElementById("kpSpm").value="";document.querySelectorAll("#kanPollForm .kp-valg").forEach(i=>i.value="");await kanPoll();}
            else alert((r&&r.fejl)||"Kunne ikke sende");}
        catch(e){}
        this.disabled=false;
    };
    const send=document.getElementById("kanSend"),txtF=document.getElementById("kanTxt");
    async function kanSendNu(){
        const txt=txtF?txtF.value.trim():"";
        if(!txt&&!kanBillede&&!(kanUp.filer&&kanUp.filer.length))return;
        if(send)send.disabled=true;
        const sess=fbaSess();
        try{const r=await apiKald({a:"kansend",token:sess.token,kid:kanValgt,txt:txt,
            billede:kanBillede||"",filer:kanUp.filer,citat:kanCitat?kanCitat.bid:""});
            if(r&&r.ok){if(txtF)txtF.value="";kanCitat=null;kanBillede=null;kanUp.filer=[];kanUp.tegn();kanCitatTegn();kanBilledeTegn();await kanPoll();}
            else alert((r&&r.fejl)||"Kunne ikke sende");}
        catch(e){alert("Kunne ikke sende — tjek forbindelsen.");}
        if(send)send.disabled=false;
    }
    if(send)send.onclick=kanSendNu;
    if(txtF)txtF.addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();kanSendNu();}});
}
var hfTagGrp=null;   /* cache: tag-grupper (m. maaBruge-flag) til kanal-vælger + @-forslag */
async function hfTagGrpHent(force){
    if(hfTagGrp&&!force)return hfTagGrp;
    try{var s=fbaSess();if(!s)return [];var j=await apiKald({a:"taggrupper",token:s.token});if(j&&j.ok)hfTagGrp=j.grupper||[];}catch(e){}
    return hfTagGrp||[];
}
function kanPanelTegn(){
    const mEl=document.getElementById("kanMedl"),iEl=document.getElementById("kanIndstil");
    if(!mEl||!iEl||!kanInfo)return;
    const k=kanInfo;
    mEl.style.display=kanMedlemVis?"":"none";
    iEl.style.display=kanIndstilVis?"":"none";
    if(kanMedlemVis){
        const kanStyre=k.ejer&&k.type!=="officiel";
        const handlingStd=k.type==="officiel"?"ejer":"tilfoej";
        mEl.innerHTML='<div style="border-top:1px solid rgba(30,55,76,.1);padding-top:10px">'+
            (k.type==="officiel"?'<p style="color:var(--graa);font-size:.8rem;margin:0 0 8px">Officiel kanal — alle administratorer er automatisk medlem. Ejere (kan skrive):</p>':'')+
            k.medlemmer.map(m=>'<span style="display:inline-flex;gap:5px;align-items:center;background:#f4f8fb;border-radius:999px;padding:4px 11px;font-size:.8rem;margin:0 6px 6px 0">'+
                (m.ejer?'⭐ ':'')+fbaEsc(m.vis)+
                (kanStyre&&m.login!==(fbaSess()||{}).u?' <button class="kan-m-fjern" data-l="'+fbaEsc(m.login)+'" title="Fjern" style="border:0;background:none;cursor:pointer;color:var(--roed);padding:0">✕</button>'+
                    (!m.ejer?' <button class="kan-m-ejer" data-l="'+fbaEsc(m.login)+'" title="Gør til kanal-ejer" style="border:0;background:none;cursor:pointer;padding:0">⭐</button>':''):'')+
                '</span>').join("")+
            ((k.taggrupper&&k.taggrupper.length)?'<div style="margin-top:4px">'+k.taggrupper.map(function(tg){return '<span style="display:inline-flex;gap:5px;align-items:center;background:#f4f8fb;border:1px solid '+fbaEsc(tg.farve||"#3cb4e5")+';border-radius:999px;padding:4px 11px;font-size:.8rem;margin:0 6px 6px 0">🏷️ '+fbaEsc(tg.navn)+' <span style="color:var(--graa)">('+tg.antal+')</span>'+(kanStyre?' <button class="kan-tg-fjern" data-g="'+fbaEsc(tg.gid)+'" title="Fjern gruppen fra kanalen" style="border:0;background:none;cursor:pointer;color:var(--roed);padding:0">✕</button>':'')+'</span>';}).join("")+'</div>':'')+
            (kanStyre||(k.ejer&&k.type==="officiel")?
                '<div style="margin-top:10px"><b style="font-size:.8rem">➕ Tilføj '+(k.type==="officiel"?"ejer":"kollega")+'</b>'+
                '<div id="kanMGrupper" style="display:flex;gap:6px;flex-wrap:wrap;margin:6px 0"></div>'+
                '<div id="kanMTagGrp" style="display:flex;gap:6px;flex-wrap:wrap;margin:0 0 6px"></div>'+
                '<input id="kanMSoeg" placeholder="🔎 Søg kollega … (fx «da» finder alle med da)" style="'+PROG_FELT+';max-width:280px;margin:0 0 6px">'+
                '<div id="kanMListe" style="display:flex;gap:6px;flex-wrap:wrap"></div></div>':'')+
            '</div>';
        const soegF=document.getElementById("kanMSoeg"),grpEl=document.getElementById("kanMGrupper"),listeEl=document.getElementById("kanMListe");
        const erMed=l=>k.medlemmer.some(m=>m.login===l);
        async function mTilf(param){
            const sess=fbaSess();
            try{const r=await apiKald(Object.assign({a:"kanmedlem",token:sess.token,kid:kanValgt,handling:handlingStd},param));
                if(r&&r.ok){kanMedlemVis=true;kanVisStroem().then(()=>{kanMedlemVis=true;kanPanelTegn();});}
                else alert((r&&r.fejl)||"Kunne ikke tilføje");}catch(e){}
        }
        function tegnVaelger(){
            if(!grpEl||!listeEl)return;
            const alleAdm=(kanData&&kanData.admins)||[];
            /* grupper først — klik tilføjer HELE gruppen */
            const GN=(typeof GRP_NAVNE!=="undefined")?GRP_NAVNE:{};
            const grupper={};
            alleAdm.forEach(x=>{if(!erMed(x.login)){const g=x.gruppe||"leder";grupper[g]=(grupper[g]||0)+1;}});
            grpEl.innerHTML=Object.keys(grupper).map(g=>'<button class="kan-m-grp" data-g="'+fbaEsc(g)+'" style="'+PROG_KNAP+';border-color:rgba(61,122,0,.4)">👥 '+fbaEsc(GN[g]||g)+' ('+grupper[g]+')</button>').join("")||'<span style="font-size:.78rem;color:var(--graa)">alle er allerede med</span>';
            grpEl.querySelectorAll(".kan-m-grp").forEach(x=>{x.onclick=()=>{
                const g=x.getAttribute("data-g");
                if(!confirm("Tilføj HELE gruppen "+(GN[g]||g)+" ("+grupper[g]+" personer)"+(handlingStd==="ejer"?" som ejere":"")+"?"))return;
                mTilf({gruppe:g});
            };});
            /* tag-grupper — klik tilføjer HELE gruppen som DYNAMISK reference (auto-opdaterer) */
            const tgEl=document.getElementById("kanMTagGrp");
            if(tgEl&&k.type!=="officiel"){
                const paaKanal={};(k.taggrupper||[]).forEach(t=>{paaKanal[t.gid]=1;});
                const ledige=(hfTagGrp||[]).filter(t=>t.maaBruge&&!paaKanal[t.gid]);
                tgEl.innerHTML=ledige.map(t=>'<button class="kan-m-tg" data-g="'+fbaEsc(t.gid)+'" style="'+PROG_KNAP+';border-color:'+fbaEsc(t.farve||"#3cb4e5")+'">🏷️ '+fbaEsc(t.navn)+' ('+t.antal+')</button>').join("");
                tgEl.querySelectorAll(".kan-m-tg").forEach(x=>{x.onclick=()=>{
                    const t=(hfTagGrp||[]).filter(z=>z.gid===x.getAttribute("data-g"))[0];
                    if(t&&!confirm("Tilføj HELE tag-gruppen “"+t.navn+"” ("+t.antal+" personer)? Medlemskabet opdaterer sig automatisk, når gruppen ændres."))return;
                    mTilf({taggruppe:x.getAttribute("data-g")});
                };});
            }
            /* kollega-søgning: «da» finder alle med da (som andre steder) */
            const soeg=(soegF&&soegF.value||"").toLowerCase().trim();
            const hits=alleAdm.filter(x=>!erMed(x.login))
                .filter(x=>!soeg||x.vis.toLowerCase().indexOf(soeg)>=0||x.login.toLowerCase().indexOf(soeg)>=0)
                .slice(0,24);
            listeEl.innerHTML=hits.map(x=>'<button class="kan-m-tilf" data-l="'+fbaEsc(x.login)+'" style="'+PROG_KNAP+'">➕ '+fbaEsc(x.vis)+'</button>').join("")||(soeg?'<span style="font-size:.78rem;color:var(--graa)">ingen fundet på «'+fbaEsc(soeg)+'»</span>':"");
            listeEl.querySelectorAll(".kan-m-tilf").forEach(x=>{x.onclick=()=>mTilf({login:x.getAttribute("data-l")});});
        }
        if(soegF){soegF.addEventListener("input",tegnVaelger);tegnVaelger();hfTagGrpHent().then(()=>tegnVaelger());}
        mEl.querySelectorAll(".kan-m-fjern").forEach(x=>{x.onclick=async()=>{
            if(!confirm("Fjern fra kanalen?"))return;const sess=fbaSess();
            try{const r=await apiKald({a:"kanmedlem",token:sess.token,kid:kanValgt,login:x.getAttribute("data-l"),handling:"fjern"});
                if(r&&r.ok){kanVisStroem().then(()=>{kanMedlemVis=true;kanPanelTegn();});}}catch(e){}
        };});
        mEl.querySelectorAll(".kan-m-ejer").forEach(x=>{x.onclick=async()=>{
            const sess=fbaSess();
            try{const r=await apiKald({a:"kanmedlem",token:sess.token,kid:kanValgt,login:x.getAttribute("data-l"),handling:"ejer"});
                if(r&&r.ok){kanVisStroem().then(()=>{kanMedlemVis=true;kanPanelTegn();});}}catch(e){}
        };});
        mEl.querySelectorAll(".kan-tg-fjern").forEach(x=>{x.onclick=async()=>{
            if(!confirm("Fjern tag-gruppen fra kanalen?"))return;const sess=fbaSess();
            try{const r=await apiKald({a:"kanmedlem",token:sess.token,kid:kanValgt,taggruppe:x.getAttribute("data-g"),handling:"fjern"});
                if(r&&r.ok){kanVisStroem().then(()=>{kanMedlemVis=true;kanPanelTegn();});}}catch(e){}
        };});
    }
    if(kanIndstilVis&&k.ejer){
        iEl.innerHTML='<div style="border-top:1px solid rgba(30,55,76,.1);padding-top:10px;display:grid;gap:8px;max-width:480px">'+
            '<input id="kiNavn" maxlength="60" value="'+fbaEsc(k.navn)+'" style="'+PROG_FELT+'">'+
            '<input id="kiBesk" maxlength="200" value="'+fbaEsc(k.besk||"")+'" placeholder="Beskrivelse" style="'+PROG_FELT+'">'+
            '<select id="kiSkriv" style="'+PROG_FELT+'"><option value="alle"'+(k.skriv!=="ejere"?" selected":"")+'>✍️ Alle medlemmer kan skrive</option>'+
            '<option value="ejere"'+(k.skriv==="ejere"?" selected":"")+'>📢 Kun kanal-ejere kan skrive</option></select>'+
            '<div style="display:flex;gap:8px;flex-wrap:wrap"><button id="kiGem" style="'+PROG_KNAP+';background:var(--groen);border:0;color:#0d2a12">💾 Gem</button>'+
            '<button id="kiArkiv" style="'+PROG_KNAP+'">'+(k.arkiv?"↩ Genåbn kanalen":"🗄 Arkivér kanalen")+'</button>'+
            '<button id="kiSlet" style="'+PROG_KNAP+';color:var(--roed)">🗑 Slet kanalen permanent</button></div></div>';
        document.getElementById("kiGem").onclick=async function(){
            this.disabled=true;const sess=fbaSess();
            try{const r=await apiKald({a:"kanret",token:sess.token,kid:kanValgt,navn:document.getElementById("kiNavn").value.trim(),
                besk:document.getElementById("kiBesk").value.trim(),skriv:document.getElementById("kiSkriv").value});
                if(r&&r.ok){kanData=null;kanVisStroem();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
        document.getElementById("kiArkiv").onclick=async function(){
            if(!confirm(k.arkiv?"Genåbn kanalen?":"Arkivér kanalen? Den lukkes for nye beskeder, men historikken bevares."))return;
            const sess=fbaSess();
            try{const r=await apiKald({a:"kanret",token:sess.token,kid:kanValgt,arkiv:k.arkiv?0:1});
                if(r&&r.ok){kanData=null;kanVisStroem();}}catch(e){}
        };
        document.getElementById("kiSlet").onclick=async function(){
            if(!confirm("SLET kanalen «"+k.navn+"» permanent?\nAlle beskeder og billeder slettes for alle — det kan IKKE fortrydes.\n(Overvej 🗄 Arkivér i stedet, hvis historikken skal bevares.)"))return;
            if(!confirm("Er du HELT sikker? Sidste chance."))return;
            this.disabled=true;const sess=fbaSess();
            try{const r=await apiKald({a:"kanslet",token:sess.token,kid:kanValgt});
                if(r&&r.ok){kanValgt=null;kanData=null;kanStart();}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke slette");}}
            catch(e){this.disabled=false;}
        };
    }
}
async function inHentOversigt(){
    const sess=fbaSess();if(!sess)return null;
    try{const j=await apiKald({a:"inoversigt",token:sess.token});return (j&&j.ok)?j:null;}catch(e){return null;}
}
async function renderIntern(){
    const wrap=document.getElementById("inWrap"),sess=fbaSess();
    if(!wrap)return;
    admMenuIndsaet("admMenuIntern","intern");
    if(!sess||!sess.token){wrap.innerHTML='<p style="color:var(--graa)">Denne side kræver administrator-login.</p>';return;}
    let fokus=sessionStorage.getItem("in_fokus");   /* åbnet via 📥 Nævnt eller toast */
    if(!fokus&&window.hfInternEt){fokus=window.hfInternEt;window.hfInternEt="";}   /* præcis ÉN ny i intern → spring direkte til den (kanal el. tråd) */
    if(fokus&&fokus.indexOf("kan:")===0){   /* nyt i en KANAL → åbn den direkte */
        sessionStorage.removeItem("in_fokus");
        inTilstand="kanaler";kanValgt=fokus.slice(4);
    }else if(fokus&&fokus.indexOf("isup:")===0){   /* nyt i en INTERN SUPPORT-sag */
        sessionStorage.removeItem("in_fokus");inTilstand="isup";window.isupFokus=fokus.slice(5);
    }else if(fokus){   /* nyt i en TRÅD → sørg for at vi er på traade-fanen (ellers overser vi den) */
        inTilstand="traade";
    }
    if(inTilstand==="isup"){isupStart();return;}
    if(inTilstand==="kanaler"){
        kanStart();
    }else{
        if(!inData){wrap.innerHTML=inFaneHTML()+'<p style="color:var(--graa)">⏳ Henter tråde …</p>';inFaneLyt(wrap);}
        const j=await inHentOversigt();
        if(!j){if(!inData)wrap.innerHTML=inFaneHTML()+'<p style="color:var(--roed)">Kunne ikke hente — kræver serveren.</p>';inFaneLyt(wrap);return;}
        inData=j;
        if(j.vismap)ADM_VIS=j.vismap;
        if(j.titelmap)ADM_TITEL=j.titelmap;
        if(fokus){sessionStorage.removeItem("in_fokus");
            const t=(j.traade||[]).find(x=>x.tid===fokus);
            if(t){inRumValgt=t.rum;inAabn(fokus);}else{inRenderListe();}}   /* tråden findes ikke længere → vis listen frem for blank */
        if(!fokus){if(inTraad){inVisTraad();}else{inRenderListe();}}
    }
    if(!inTimer)inTimer=setInterval(async ()=>{
        if(location.hash.indexOf("intern")<0){clearInterval(inTimer);inTimer=null;return;}
        inTikTael++;
        if(inTilstand==="kanaler"){
            const aktivK=document.activeElement;
            if(aktivK&&(aktivK.tagName==="INPUT"||aktivK.tagName==="TEXTAREA")&&!kanValgt)return;
            if(kanValgt){await kanPoll();return;}
            if(inTikTael%3)return;   /* kanal-listen hver ~18. sekund */
            const foer=JSON.stringify(kanData&&kanData.kanaler);
            const nyt=await kanHentListe();
            if(nyt&&JSON.stringify(nyt.kanaler)!==foer&&!kanOpretVis)kanRenderListe();
            return;
        }
        if(inTraad){await inPollTraad();return;}
        if(inTikTael%3)return;   /* oversigten hver ~18. sekund */
        const aktiv=document.activeElement;
        if(aktiv&&(aktiv.tagName==="INPUT"||aktiv.tagName==="TEXTAREA"))return;
        const nyt=await inHentOversigt();
        if(nyt){const foer=JSON.stringify(inData&&inData.traade);inData=nyt;
            if(JSON.stringify(nyt.traade)!==foer)inRenderListe();}
    },6000);
}
function inRenderListe(){
    const wrap=document.getElementById("inWrap"),sess=fbaSess();
    if(!wrap||!inData)return;
    const rum=inData.rum||{},traade=(inData.traade||[]);
    if(!rum[inRumValgt])inRumValgt=Object.keys(rum)[0]||"gen";
    const erSoren=!!inData.ejer;
    /* rum-chips m. ulæst-tællere */
    let html=inFaneHTML()+'<div class="fba-bar">'+Object.keys(rum).map(id=>{
            const n=traade.filter(t=>t.rum===id&&t.ulaest&&!t.arkiv).length;
            return '<button class="in-rum'+(id===inRumValgt?' aktiv':'')+'" data-r="'+fbaEsc(id)+'">'+fbaEsc(rum[id])+
                (n?' <span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+n+'</span>':'')+'</button>';
        }).join("")+
        (erSoren?'<button id="inRumNy" title="Opret nyt rum">➕ Nyt rum</button>':'')+
        '<label class="fba-vis"><input type="checkbox" id="inArkivTgl" '+(inVisArkiv?"checked":"")+'> Vis arkiv</label>'+
        '<button id="inNyTraad">✍️ Ny tråd</button></div>';
    /* opret-formular */
    if(inOpretVis){
        html+='<div class="fba-kort" style="border-left:5px solid var(--blaa)"><b>✍️ Ny tråd i '+fbaEsc(rum[inRumValgt])+'</b>'+
            '<div style="display:grid;gap:10px;margin-top:10px">'+
            '<input id="inEmne" class="nya-felt" type="text" maxlength="120" placeholder="Emne">'+
            '<div style="position:relative"><textarea id="inFoerste" class="nya-felt" rows="3" maxlength="2000" placeholder="Første besked … @navn tilføjer en kollega til tråden" style="width:100%;box-sizing:border-box"></textarea>'+
            '<div id="inOpretForslag" style="position:absolute;bottom:calc(100% + 4px);left:0;display:none;gap:6px;flex-wrap:wrap;background:#fff;border-radius:10px;box-shadow:var(--skygge);padding:6px;z-index:5"></div></div>'+
            '<div style="display:flex;gap:10px">'+
            '<button id="inOpretOk" class="fba-send" style="border:0;border-radius:11px;padding:11px 18px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.9rem">Opret tråden</button>'+
            '<button id="inOpretNej" style="border:0;background:none;color:var(--graa);cursor:pointer;font-family:var(--font);font-size:.85rem">Annullér</button>'+
            '</div></div></div>';
    }
    /* søge-/filter-bar + tråd-liste (listen fyldes af inListeFyld, så søgefeltet beholder fokus) */
    const delt={};traade.forEach(t=>(t.deltagere||[]).forEach(d=>{delt[d]=1;}));
    const kolleger=Object.keys(delt).sort((a,b)=>String(visA(a)).localeCompare(String(visA(b)),'da'));
    html+='<div class="fba-bar" style="margin:2px 0 12px">'+
        '<input id="inSoegFelt" type="search" placeholder="🔍 Søg i emne eller deltager …" value="'+fbaEsc(inSoeg)+'" style="flex:1;min-width:150px;border:2px solid #dbe6ee;border-radius:999px;padding:8px 14px;font-family:var(--font);font-size:.88rem;outline:none">'+
        '<select id="inFiltDelt" style="'+PROG_FELT+';max-width:190px"><option value="">👥 Alle medarbejdere</option>'+
        kolleger.map(l=>'<option value="'+fbaEsc(l)+'"'+(inFilterDelt===l?' selected':'')+'>'+fbaEsc(visA(l))+'</option>').join("")+'</select>'+
        '<label class="fba-vis" style="margin-left:0"><input type="checkbox" id="inKunUl" '+(inKunUlaest?"checked":"")+'> Kun ulæste</label>'+
        '<label class="fba-vis" style="margin-left:0"><input type="checkbox" id="inKunNv" '+(inKunNaevnt?"checked":"")+'> Hvor jeg er nævnt</label>'+
        '</div>'+
        '<div id="inTraadListe"></div>';
    wrap.innerHTML=html;
    inListeFyld();
    /* lyttere */
    inFaneLyt(wrap);
    wrap.querySelectorAll(".in-rum").forEach(b=>{b.onclick=()=>{inRumValgt=b.getAttribute("data-r");inOpretVis=false;inRenderListe();};});
    document.getElementById("inArkivTgl").onchange=e=>{inVisArkiv=e.target.checked;inRenderListe();};
    document.getElementById("inNyTraad").onclick=()=>{inOpretVis=!inOpretVis;inRenderListe();};
    const rumNy=document.getElementById("inRumNy");
    if(rumNy)rumNy.onclick=async ()=>{
        const navn=(window.prompt("Navn på det nye rum (fx «📦 Indkøb»):")||"").trim();
        if(!navn)return;
        try{const r=await apiKald({a:"inrumny",token:sess.token,navn:navn});
            if(r&&r.ok){const nyt=await inHentOversigt();if(nyt){inData=nyt;inRumValgt=r.id;inRenderListe();}}
            else alert((r&&r.fejl)||"Kunne ikke oprette rummet");}catch(e){}
    };
    const sf=document.getElementById("inSoegFelt");if(sf)sf.oninput=()=>{inSoeg=sf.value;inListeFyld();inSoegIndhold();};
    const fd=document.getElementById("inFiltDelt");if(fd)fd.onchange=()=>{inFilterDelt=fd.value;inListeFyld();};
    const ku=document.getElementById("inKunUl");if(ku)ku.onchange=()=>{inKunUlaest=ku.checked;inListeFyld();};
    const kn=document.getElementById("inKunNv");if(kn)kn.onchange=()=>{inKunNaevnt=kn.checked;inListeFyld();};
    if(inOpretVis){
        atForslag(document.getElementById("inFoerste"),document.getElementById("inOpretForslag"),GRP_TAG_LISTE.concat(inData.admins||[]));
        document.getElementById("inOpretNej").onclick=()=>{inOpretVis=false;inRenderListe();};
        document.getElementById("inOpretOk").onclick=async function(){
            const emne=document.getElementById("inEmne").value.trim(),txt=document.getElementById("inFoerste").value.trim();
            if(!emne||!txt){alert("Udfyld både emne og besked.");return;}
            this.disabled=true;
            try{const r=await apiKald({a:"inopret",token:sess.token,rum:inRumValgt,emne:emne,txt:txt});
                if(r&&r.ok){inOpretVis=false;const nyt=await inHentOversigt();if(nyt)inData=nyt;inAabn(r.tid);}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke oprette");}}
            catch(e){this.disabled=false;}
        };
    }
}
function inSoegIndhold(){   /* #6: søg også i selve beskedteksten (server), debounced */
    const q=(inSoeg||"").trim();
    if(inSoegTimer)clearTimeout(inSoegTimer);
    if(q.length<2){if(inSoegTraf.length){inSoegTraf=[];inListeFyld();}return;}
    inSoegTimer=setTimeout(async ()=>{
        const sess=fbaSess();if(!sess)return;
        try{const r=await apiKald({a:"insoeg",token:sess.token,q:q});
            if(r&&r.ok){inSoegTraf=r.traade||[];inListeFyld();}}catch(e){}
    },350);
}
function inListeFyld(){
    const el=document.getElementById("inTraadListe");if(!el||!inData)return;
    const traade=inData.traade||[];
    const q=(inSoeg||"").trim().toLowerCase();
    let liste=traade.filter(t=>t.rum===inRumValgt&&(inVisArkiv?t.arkiv:!t.arkiv));
    if(inKunUlaest)liste=liste.filter(t=>t.ulaest);
    if(inKunNaevnt)liste=liste.filter(t=>t.naevnt);
    if(inFilterDelt)liste=liste.filter(t=>(t.deltagere||[]).indexOf(inFilterDelt)>=0);
    if(q)liste=liste.filter(t=>String(t.emne||"").toLowerCase().indexOf(q)>=0||(t.deltagere||[]).some(d=>String(visA(d)).toLowerCase().indexOf(q)>=0)||inSoegTraf.indexOf(t.tid)>=0);
    liste=liste.slice().sort((a,b)=>b.sidst-a.sidst);
    const aktive=liste.filter(t=>!t.haandteret),haandt=liste.filter(t=>t.haandteret);
    const kort=t=>'<div class="fba-kort in-traad" data-t="'+fbaEsc(t.tid)+'" style="cursor:pointer'+(t.ulaest?';border-left:5px solid var(--roed)':(t.haandteret?';border-left:5px solid var(--groen);opacity:.8':''))+'">'+
        '<div class="fba-top"><span style="font-weight:800;display:inline-flex;align-items:center;gap:8px">'+
        (t.naevnt
            ?'<span title="Du er nævnt her — ikke set endnu" style="flex:none;color:var(--roed);font-weight:900;font-size:1.02rem;line-height:1">@</span>'
            :(t.ulaest?'<span title="Nyt du ikke har læst" style="flex:none;width:9px;height:9px;border-radius:50%;background:var(--roed)"></span>'
                :(t.haandteret?'<span style="flex:none;color:var(--groen)">✅</span>':'')))+
        fbaEsc(t.emne)+'</span>'+
        deltStakHTML(t.deltagere,true,false)+
        '<button class="in-haandt" data-t="'+fbaEsc(t.tid)+'" data-til="'+(t.haandteret?0:1)+'" title="'+(t.haandteret?'Fjern håndteret':'Markér som håndteret')+'" style="margin-left:auto;border:1px solid rgba(30,55,76,.16);background:#fff;border-radius:999px;font-family:var(--font);font-size:.7rem;font-weight:800;color:'+(t.haandteret?'#41630a':'var(--graa)')+';padding:2px 9px;cursor:pointer">'+(t.haandteret?'✅ Håndteret':'✓ Håndteret')+'</button>'+
        '<span style="color:var(--graa);font-size:.8rem;white-space:nowrap">'+fbaDato(t.sidst)+(t.arkiv?' · 🗄':'')+'</span></div></div>';
    let h="";
    if(aktive.length)h+=aktive.map(kort).join("");
    else if(!haandt.length)h+='<p style="color:var(--graa)">'+((q||inFilterDelt||inKunUlaest||inKunNaevnt)?'Ingen tråde matcher filteret.':(inVisArkiv?'Intet i arkivet her.':'Ingen tråde i dette rum endnu — start den første ✍️'))+'</p>';
    if(haandt.length)h+='<details class="tf-fold" style="margin-top:12px"><summary class="tf-sum">✅ '+haandt.length+' håndteret'+(haandt.length===1?' tråd':'e tråde')+' <span class="tf-vis">— vis / skjul</span></summary><div style="padding:8px 4px 2px">'+haandt.map(kort).join("")+'</div></details>';
    el.innerHTML=h;
    if(typeof hfAvatarWire==="function")hfAvatarWire(el);
    el.querySelectorAll(".in-traad").forEach(k=>{k.onclick=(e)=>{if(e.target&&e.target.classList&&e.target.classList.contains("in-haandt"))return;inAabn(k.getAttribute("data-t"));};});
    el.querySelectorAll(".in-haandt").forEach(b=>{b.onclick=async(e)=>{e.stopPropagation();const sess=fbaSess();const tid=b.getAttribute("data-t"),til=b.getAttribute("data-til")==="1";
        const t=(inData.traade||[]).find(x=>x.tid===tid);if(t)t.haandteret=til?1:0;inListeFyld();
        try{await apiKald({a:"inhaandteret",token:sess.token,tid:tid,til:til?1:0});}catch(err){}
    };});
}
function inAabn(tid){inTraad=tid;inEfter=0;
    var _t=((inData&&inData.traade)||[]).find(function(x){return x.tid===tid;});
    if(_t){_t.ulaest=0;_t.naevnt=0;}   /* ryd markering straks (serveren rydder @-nævningen i inhent) */
    inVisTraad();}
/* ===== deltager-visning i private tråde: avatar-stak + «hvem kan se tråden»-popup ===== */
var inDeltagereListe=[];
function deltStakHTML(deltagere,lys,klik){
    var d=deltagere||[],px=24,maks=5,vis=d.slice(0,maks),rest=d.length-vis.length;
    var av=vis.map(function(l,ix){
        var u=encodeURIComponent(String(l).toLowerCase());
        var init=fbaEsc(String((typeof visA==="function"?visA(l):l)||l).substring(0,1).toUpperCase());
        return '<span style="position:relative;width:'+px+'px;height:'+px+'px;border-radius:50%;overflow:hidden;border:2px solid #fff;background:linear-gradient(120deg,var(--blaa),#77c9ec);color:#fff;font-weight:800;display:inline-grid;place-items:center;font-size:10px;margin-left:'+(ix?-8:0)+'px;flex:none">'+init+'<img class="hf-av-img" src="mithf-api.php?a=pfoto&u='+u+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0"></span>';
    }).join("");
    var stak='<span style="display:inline-flex;align-items:center;vertical-align:middle">'+av+(rest>0?'<span style="width:'+px+'px;height:'+px+'px;border-radius:50%;border:2px solid #fff;background:#4a6b82;color:#fff;font-weight:800;display:inline-grid;place-items:center;font-size:10px;margin-left:-8px;flex:none">+'+rest+'</span>':'')+'</span>';
    var lbl='<span style="font-size:.78rem;font-weight:700;color:'+(lys?'var(--graa)':'#fff')+'">'+d.length+' deltager'+(d.length===1?'':'e')+'</span>';
    var indre=stak+lbl;
    if(klik)return '<button class="delt-stak" title="Se hvem der kan se tråden" style="display:inline-flex;align-items:center;gap:8px;border:0;background:'+(lys?'rgba(30,55,76,.06)':'rgba(255,255,255,.14)')+';border-radius:999px;padding:3px 12px 3px 5px;cursor:pointer;font-family:var(--font)">'+indre+'</button>';
    return '<span style="display:inline-flex;align-items:center;gap:8px">'+indre+'</span>';
}
function inDeltRender(){
    var d=document.getElementById("inDeltagere");if(!d)return;
    d.innerHTML=deltStakHTML(inDeltagereListe,false,true);
    if(typeof hfAvatarWire==="function")hfAvatarWire(d);
    var btn=d.querySelector(".delt-stak");
    if(btn)btn.onclick=function(){aabnDeltagere(inDeltagereListe);};
}
function aabnDeltagere(deltagere){
    var d=(deltagere||[]).slice().sort(function(a,b){return String(visA(a)).localeCompare(String(visA(b)),"da");});
    var ov=document.getElementById("dtOverlay");
    if(!ov){ov=document.createElement("div");ov.id="dtOverlay";
        ov.style.cssText="position:fixed;inset:0;z-index:999;background:rgba(30,55,76,.5);display:flex;align-items:center;justify-content:center;padding:18px";
        ov.addEventListener("click",function(e){if(e.target===ov)ov.remove();});
        document.body.appendChild(ov);}
    var sess=fbaSess();
    ov.innerHTML='<div style="width:min(420px,100%);max-height:80vh;display:flex;flex-direction:column;background:#fff;border-radius:22px;box-shadow:0 30px 80px rgba(0,0,0,.4);overflow:hidden;font-family:var(--font)">'+
        '<div style="padding:18px 22px 10px;display:flex;align-items:center;gap:10px"><span style="font-size:1.3rem;flex:none">👥</span><div style="flex:1;min-width:0"><b style="font-size:1.05rem">Hvem kan se tråden</b><div style="color:var(--graa);font-size:.8rem">'+d.length+' deltager'+(d.length===1?'':'e')+'</div></div><button id="dtLuk" style="border:0;background:rgba(30,55,76,.08);border-radius:10px;width:32px;height:32px;cursor:pointer;font-size:.95rem;flex:none">✕</button></div>'+
        '<div style="flex:1;overflow-y:auto;padding:2px 14px 16px">'+
        d.map(function(l){
            var u=encodeURIComponent(String(l).toLowerCase());
            var init=fbaEsc(String(visA(l)||l).substring(0,1).toUpperCase());
            var mig=(sess&&sess.u===l);
            return '<div style="display:flex;gap:11px;align-items:center;padding:8px 10px;border-radius:12px">'+
                '<span style="position:relative;width:34px;height:34px;border-radius:50%;overflow:hidden;flex:none;background:linear-gradient(120deg,var(--blaa),#77c9ec);color:#fff;font-weight:800;display:grid;place-items:center;font-size:.85rem">'+init+'<img class="hf-av-img" src="mithf-api.php?a=pfoto&u='+u+'" alt="" style="position:absolute;inset:0;width:100%;height:100%;object-fit:cover;opacity:0"></span>'+
                '<div style="flex:1;min-width:0"><b style="font-size:.9rem">'+fbaEsc(visA(l))+(mig?' <span style="color:var(--graa);font-weight:400">(dig)</span>':'')+'</b>'+(typeof visTitelHTML==="function"?visTitelHTML(l):'')+'</div></div>';
        }).join("")+
        '</div></div>';
    document.getElementById("dtLuk").onclick=function(){ov.remove();};
    if(typeof hfAvatarWire==="function")hfAvatarWire(ov);
}
function inVisTraad(){
    const wrap=document.getElementById("inWrap"),sess=fbaSess();
    if(!wrap||!inTraad)return;
    inEfter=0;   /* loggen gen-tegnes → hent alt igen */
    const t=(inData.traade||[]).find(x=>x.tid===inTraad);
    wrap.innerHTML='<div class="fba-kort" style="padding:0;overflow:hidden">'+
        '<div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;padding:14px 18px;background:linear-gradient(120deg,var(--moerk),#2a5573);color:#fff">'+
        '<button id="inTilbage" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:9px;padding:7px 12px;cursor:pointer;font-family:var(--font);font-weight:700">‹ Tråde</button>'+
        '<div style="flex:1;min-width:200px"><b id="inTitel">'+fbaEsc(t?t.emne:"")+'</b>'+
        '<div id="inDeltagere" style="margin-top:5px"></div></div>'+
        '<button id="inHopNyt" title="Hop til det nye (første ulæste)" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:10px;padding:9px 12px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">↓ Nyt</button>'+
        '<button id="inArkivKnap" style="border:0;background:rgba(255,255,255,.15);color:#fff;border-radius:10px;padding:9px 14px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">'+(t&&t.arkiv?'↩ Genåbn':'🗄 Arkivér')+'</button>'+
        '</div>'+
        '<div id="inLog" style="height:52vh;overflow-y:auto;padding:16px;background:var(--flade)"><p style="color:var(--graa)">⏳ Henter …</p></div>'+
        '<div style="display:flex;gap:10px;padding:12px 16px;border-top:1px solid rgba(30,55,76,.08);background:#fff;position:relative">'+
        '<div id="inVedhVis" style="position:absolute;bottom:calc(100% + 4px);right:16px;left:16px;display:flex;gap:8px;flex-wrap:wrap;z-index:4"></div>'+
        '<textarea id="inSvar" rows="1" maxlength="2000" placeholder="Skriv en besked … @navn tilføjer en kollega" style="flex:1;border:2px solid #dbe6ee;border-radius:18px;padding:11px 16px;font-family:var(--font);font-size:.92rem;outline:none;resize:none;line-height:1.35;max-height:120px;overflow-y:auto"></textarea>'+
        '<button id="inVedh" title="Vedhæft fil (Word, Excel, PDF, billede …)" style="border:0;background:#eef4f8;border-radius:50%;width:44px;height:44px;cursor:pointer;font-size:1.05rem">📎</button>'+
        hfEmojiKnap("inSvar")+
        '<button id="inSend" class="fba-send" style="border:0;border-radius:50%;width:44px;height:44px;cursor:pointer;font-size:1.05rem">➤</button>'+
        '<div id="inForslag" style="position:absolute;bottom:calc(100% + 4px);left:16px;display:none;gap:6px;flex-wrap:wrap;background:#fff;border-radius:10px;box-shadow:var(--skygge);padding:6px;z-index:5"></div>'+
        '</div></div>';
    document.getElementById("inTilbage").onclick=async ()=>{inTraad=null;const nyt=await inHentOversigt();if(nyt)inData=nyt;inRenderListe();};
    document.getElementById("inHopNyt").onclick=()=>tfHopUlaest("inLog");
    document.getElementById("inArkivKnap").onclick=async function(){
        const tt=(inData.traade||[]).find(x=>x.tid===inTraad);
        const til=tt&&tt.arkiv?0:1;
        this.disabled=true;
        try{const r=await apiKald({a:"inarkiv",token:sess.token,tid:inTraad,arkiv:til});
            if(r&&r.ok){if(tt)tt.arkiv=til;inTraad=null;const nyt=await inHentOversigt();if(nyt)inData=nyt;inRenderListe();}
            else this.disabled=false;}
        catch(e){this.disabled=false;}
    };
    const svar=document.getElementById("inSvar"),send=document.getElementById("inSend");
    const inUp=supUploader(document.getElementById("inVedhVis"),{token:sess.token});
    const inVedhBtn=document.getElementById("inVedh");if(inVedhBtn)inVedhBtn.onclick=()=>inUp.vaelg();
    atForslag(svar,document.getElementById("inForslag"),GRP_TAG_LISTE.concat(inData.admins||[]));
    async function sendBesked(){
        const v=svar.value.trim();if(!v&&!(inUp.filer&&inUp.filer.length))return;
        const filer=(inUp.filer||[]).slice();
        svar.value="";svar.style.height="auto";inUp.filer=[];inUp.tegn();
        const optim={af:sess.u,txt:v,filer:filer,ts:Math.floor(Date.now()/1000)};
        inBeskeder.push(optim);inLogTegn();
        try{const r=await apiKald({a:"inbesked",token:sess.token,tid:inTraad,txt:v,filer:filer});
            if(!(r&&r.ok))alert((r&&r.fejl)||"Beskeden kom ikke frem");
            else{
                if(r.bid){optim.bid=r.bid;if(r.ts)optim.ts=r.ts;inEfter=Math.max(inEfter,optim.ts||0);inLogTegn();}  /* giv beskeden dens id straks → ✏️ Redigér virker uden at genåbne tråden */
                if(r.nyeDeltagere&&r.nyeDeltagere.length){
                    r.nyeDeltagere.forEach(function(l){if(inDeltagereListe.indexOf(l)<0)inDeltagereListe.push(l);});
                    inDeltRender();}}}
        catch(e){alert("Beskeden kom ikke frem — tjek forbindelsen.");}
    }
    send.onclick=sendBesked;
    svar.addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey&&document.getElementById("inForslag").style.display!=="flex"){e.preventDefault();sendBesked();}});
    svar.addEventListener("input",()=>hfGrow(svar,120));
    inDeltagereListe=(t&&t.deltagere)?t.deltagere.slice():[];inDeltRender();
    inPollTraad(true);
}
function inBeskedHTML(b){
    const ts=b.ts||0;
    const _linje=mrkLinje("t",b,visA(b.af||""),(b.txt||""));if(_linje)return _linje;
    /* samme visning som kanaler: avatar + navn + tid + tekst + markér-knapper (ingen venstre/højre-bobler) */
    return '<div style="display:flex;gap:10px;margin:0 0 14px">'+
        hfAvatarHTML(b.af,visA(b.af||""))+
        '<div style="flex:1;min-width:0">'+
        '<div style="font-size:.78rem;color:var(--graa)"><b style="color:var(--moerk)">'+fbaEsc(visA(b.af||""))+'</b>'+(typeof visTitelHTML==="function"?visTitelHTML(b.af||""):'')+' · '+fbaDato(ts)+(b.redigeret?' · <span style="font-style:italic" title="Redigeret '+fbaDato(b.redigeret)+'">redigeret</span>':'')+'</div>'+
        (b.txt?'<div class="besk-txt" style="white-space:pre-wrap;word-break:break-word;font-size:.92rem;margin-top:2px">'+inTagHTML(b.txt||"")+'</div>':'')+
        (b.filer&&b.filer.length?supFilerHTML(b.filer,false):'')+
        '<div style="margin-top:5px;display:flex;gap:6px;align-items:center">'+mrkKnapper("t",b)+(beskMaaRedigere(b)?'<button class="besk-ret" data-kilde="in" data-b="'+fbaEsc(b.bid)+'" style="border:0;background:none;cursor:pointer;font-size:.75rem;color:var(--graa);font-family:var(--font);font-weight:700">✏️ Redigér</button>':'')+'</div>'+
        '</div></div>';
}
function inLogTegn(){
    const log=document.getElementById("inLog");if(!log)return;
    beskRedEgne=!!inMaaRed;beskRedMod=!!inMaaMod;
    const iBunden=log.scrollHeight-log.scrollTop-log.clientHeight<110||!log.dataset.tegnet;
    if(!inBeskeder.length){log.innerHTML='<p style="color:var(--graa)">Ingen beskeder endnu 👇</p>';log.dataset.tegnet="1";return;}
    /* samme metodik som kanaler: delt fold (tfRenderHTML) + samme markering + avatar-billeder */
    log.innerHTML=tfRenderHTML(inBeskeder,inLaestTil||0,inBeskedHTML);
    log.dataset.tegnet="1";
    tfWire(log,inSaetLaest);
    mrkWire(log);
    hfAvatarWire(log);
    hfAvatarTagWire(log,"inSvar");
    beskRetWire(log,"in");
    if(iBunden&&!tfOpen)log.scrollTop=log.scrollHeight;
}
async function inSaetLaest(ts){
    const sess=fbaSess();if(!sess||!inTraad)return;
    inLaestTil=ts;tfOpen=false;inLogTegn();
    try{await apiKald({a:"inlaest",token:sess.token,tid:inTraad,ts:ts});}catch(e){}
}
async function inPollTraad(foerste){
    if(!inTraad)return;
    const sess=fbaSess();if(!sess)return;
    if(foerste){inBeskeder=[];inEfter=0;beskedMrk={};beskedUdvid.clear();}
    try{
        const j=await apiKald({a:"inhent",token:sess.token,tid:inTraad,efter:inEfter});
        if(!(j&&j.ok))return;
        if(j.maaRed!==undefined)inMaaRed=!!j.maaRed;if(j.maaMod!==undefined)inMaaMod=!!j.maaMod;
        if(j.laestTil!==undefined)inLaestTil=j.laestTil||0;
        const mFoer=JSON.stringify(beskedMrk);if(j.mrk)Object.assign(beskedMrk,j.mrk);
        let nye=(JSON.stringify(beskedMrk)!==mFoer);
        (j.beskeder||[]).forEach(b=>{
            inEfter=Math.max(inEfter,b.ts||0);
            const key=(b.ts||0)+"|"+(b.af||"")+"|"+(b.txt||"");
            if(inBeskeder.some(x=>((x.ts||0)+"|"+(x.af||"")+"|"+(x.txt||""))===key))return;
            inBeskeder.push(b);nye=true;
        });
        if(nye||foerste)inLogTegn();
        if(j.deltagere){inDeltagereListe=j.deltagere.slice();inDeltRender();}
    }catch(e){}
}

/* ===================== 📊 Tilfredshed (admin — perm 'stat') ===================== */
/* ===== tilfredshedsmålinger — admin (perm 'stat') ===== */
let statValgtMid=null,statTekstSort="ny",statEdRedigerer=false;
const STAT_SMIL=["😞","😕","😐","🙂","😍"];
const STAT_FARVE=["#e7004c","#f5842f","#f2c500","#93d500","#2fa84f"];
function statDato(ts){return ts?fbaDato(ts):"";}
function statDonut(ford,snit,n){   /* cirkeldiagram (donut) over 1-5-fordelingen, snit i midten */
    const vals=[1,2,3,4,5].map(v=>(ford&&ford[v])||0);
    const total=vals.reduce((a,b)=>a+b,0);
    const cx=64,cy=64,R=54,r0=33;
    let inner='<circle cx="64" cy="64" r="'+((R+r0)/2)+'" fill="none" stroke="rgba(30,55,76,.07)" stroke-width="'+(R-r0)+'"/>';
    if(total){
        inner="";let ang=-Math.PI/2;
        vals.forEach((cnt,ix)=>{
            if(!cnt)return;
            const frac=cnt/total;
            if(frac>=0.9999){inner+='<circle cx="64" cy="64" r="'+((R+r0)/2)+'" fill="none" stroke="'+STAT_FARVE[ix]+'" stroke-width="'+(R-r0)+'"/>';return;}
            const a2=ang+frac*2*Math.PI,large=frac>0.5?1:0;
            const x1=cx+R*Math.cos(ang),y1=cy+R*Math.sin(ang),x2=cx+R*Math.cos(a2),y2=cy+R*Math.sin(a2);
            const xi1=cx+r0*Math.cos(ang),yi1=cy+r0*Math.sin(ang),xi2=cx+r0*Math.cos(a2),yi2=cy+r0*Math.sin(a2);
            inner+='<path d="M'+x1.toFixed(2)+' '+y1.toFixed(2)+' A'+R+' '+R+' 0 '+large+' 1 '+x2.toFixed(2)+' '+y2.toFixed(2)+' L'+xi2.toFixed(2)+' '+yi2.toFixed(2)+' A'+r0+' '+r0+' 0 '+large+' 0 '+xi1.toFixed(2)+' '+yi1.toFixed(2)+' Z" fill="'+STAT_FARVE[ix]+'"/>';
            ang=a2;
        });
    }
    return '<svg viewBox="0 0 128 128" width="128" height="128" style="flex:none">'+inner+
        '<text x="64" y="60" text-anchor="middle" font-size="26" font-weight="800" fill="var(--blaa)" font-family="var(--font)">'+(total?snit.toFixed(1):"—")+'</text>'+
        '<text x="64" y="80" text-anchor="middle" font-size="11" fill="#7a8ea0" font-family="var(--font)">'+total+' svar</text></svg>';
}
async function renderStat(){
    const liste=document.getElementById("fbaListe"),sess=fbaSess();
    if(!liste||!sess)return;
    if(!statEdRedigerer)liste.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    let j=null;
    try{j=await apiKald({a:"maalingadmin",token:sess.token,mid:statValgtMid||""});}catch(e){}
    if(!(j&&j.ok)){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
    statEdRedigerer=false;
    const v=j.valgt;   /* den valgte målings detaljer (kan være null) */
    if(v)statValgtMid=v.id;
    /* ── topkort: overblik + ny måling ── */
    let html='<div class="fba-kort" style="display:flex;gap:14px;align-items:center;flex-wrap:wrap">'+
        '<span style="font-size:1.5rem">📊</span>'+
        '<div style="flex:1;min-width:200px"><b style="font-size:1.05rem">Tilfredshedsundersøgelser</b>'+
        '<div style="color:var(--graa);font-size:.85rem">'+((j.liste||[]).length)+' måling'+(((j.liste||[]).length)===1?'':'er') +' i alt · '+(j.aktivId?'én er åben nu':'ingen er åbne lige nu')+' · alle svar er anonyme</div></div>'+
        '<button id="statNy" class="fba-send" style="border:0;border-radius:12px;padding:10px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">➕ Åbn ny måling</button></div>';
    /* ── målings-vælger (chips) ── */
    html+='<div class="fba-kort"><div style="font-weight:800;font-size:.82rem;color:var(--graa);margin-bottom:8px">VÆLG MÅLING</div>'+
        '<div style="display:flex;gap:8px;flex-wrap:wrap">'+
        (j.liste||[]).map(m=>{const valgt=(v&&v.id===m.id);
            return '<button class="stat-vaelg" data-mid="'+m.id+'" style="border:2px solid '+(valgt?'var(--blaa)':'rgba(30,55,76,.14)')+';background:'+(valgt?'#eaf6fd':'#fff')+';border-radius:12px;padding:8px 12px;cursor:pointer;font-family:var(--font);text-align:left;min-width:150px">'+
                '<div style="font-weight:700;font-size:.86rem">'+fbaEsc(m.titel)+(m.aktiv?' <span style="color:var(--groen)">●</span>':'')+'</div>'+
                '<div style="color:var(--graa);font-size:.74rem">'+statDato(m.aabnet)+(m.aktiv?' – åben':(m.lukket?' – lukket '+statDato(m.lukket):' – afsluttet'))+' · '+m.antal+' svar</div></button>';
        }).join("")+'</div></div>';
    if(!v){liste.innerHTML=html+'<div class="fba-kort"><p style="color:var(--graa);margin:0">Vælg en måling ovenfor for at se resultater.</p></div>';statWire(j,sess);return;}
    /* ── valgt målings hoved ── */
    html+='<div class="fba-kort" style="border-left:5px solid '+(v.aktiv?'var(--groen)':'#9db4c4')+';display:flex;gap:14px;align-items:center;flex-wrap:wrap">'+
        '<div style="flex:1;min-width:200px"><b style="font-size:1.05rem">'+fbaEsc(v.titel)+'</b> '+(v.aktiv?'<span class="fba-side" style="background:rgba(147,213,0,.2);color:#41630a">ÅBEN</span>':'<span class="fba-side" style="background:rgba(30,55,76,.08)">Lukket</span>')+
        '<div style="color:var(--graa);font-size:.85rem;margin-top:2px">'+statDato(v.aabnet)+(v.aktiv?' – åben nu':(v.lukket?' – lukket '+statDato(v.lukket):' – afsluttet'))+' · <b>'+v.antal+'</b> anonyme besvarelser</div></div>'+
        (v.aktiv?'<button id="statLuk" class="fba-done" style="border:0;border-radius:12px;padding:9px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem">🔒 Luk målingen</button>':'')+'</div>';
    /* ── skala-spørgsmål: cirkeldiagram + fordeling ── */
    (v.spm||[]).forEach(s=>{
        if(s.type!=="skala")return;
        const st=(v.stats&&v.stats[s.id])||{snit:0,ford:{},n:0};
        html+='<div class="fba-kort"><b>'+fbaEsc(s.t)+'</b>'+
            '<div style="display:flex;gap:18px;align-items:center;margin-top:8px;flex-wrap:wrap">'+
            statDonut(st.ford,st.snit,st.n)+
            '<div style="flex:1;min-width:180px;display:grid;gap:4px">'+
            [5,4,3,2,1].map(vv=>{const n2=(st.ford&&st.ford[vv])||0,pct=st.n?Math.round(n2*100/st.n):0;
                return '<div style="display:flex;align-items:center;gap:8px;font-size:.8rem">'+
                    '<span style="width:22px;text-align:center">'+STAT_SMIL[vv-1]+'</span>'+
                    '<div style="flex:1;height:12px;border-radius:99px;background:rgba(30,55,76,.07)"><div style="height:12px;border-radius:99px;width:'+pct+'%;background:'+STAT_FARVE[vv-1]+'"></div></div>'+
                    '<span style="width:64px;text-align:right;color:var(--graa)">'+n2+' ('+pct+'%)</span></div>';
            }).join("")+'</div></div></div>';
    });
    /* ── fritekst-svar: sorterbare ── */
    const spmNavn={};(v.spm||[]).forEach(s=>{spmNavn[s.id]=s.t;});
    let tekster=(v.tekster||[]).slice();
    tekster.sort((a,b)=>statTekstSort==="ny"?(b.ts-a.ts):(a.ts-b.ts));
    html+='<div class="fba-kort"><div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-bottom:6px">'+
        '<b style="flex:1">💬 Fritekst-svar ('+tekster.length+')</b>'+
        '<select id="statSort" style="border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:6px 10px;font-family:var(--font);font-size:.82rem"><option value="ny"'+(statTekstSort==="ny"?" selected":"")+'>Nyeste først</option><option value="gl"'+(statTekstSort==="gl"?" selected":"")+'>Ældste først</option></select></div>'+
        (tekster.map(t=>'<div style="background:#fff;border-radius:10px;padding:9px 12px;margin-top:6px;border:1px solid rgba(30,55,76,.07);font-size:.88rem">'+
            '<div style="color:var(--graa);font-size:.72rem;margin-bottom:2px">'+fbaDato(t.ts)+(spmNavn[t.sid]?' · '+fbaEsc(spmNavn[t.sid]):'')+'</div>'+fbaEsc(t.txt)+'</div>').join("")||'<p style="color:var(--graa);margin:6px 0 0">Ingen fritekst-svar endnu.</p>')+'</div>';
    /* ── redigér spørgsmål (kun den ÅBNE måling kan ændres) ── */
    if(!v.aktiv){
        html+='<div class="fba-kort" style="color:var(--graa);font-size:.85rem">🔒 Denne måling er afsluttet, så spørgsmålene kan ikke ændres. Åbn en ny måling for at stille nye spørgsmål (du kan kopiere spørgsmålene herfra).</div>';
    }else{
        html+='<details class="fba-kort" id="statEdBox"><summary style="cursor:pointer;font-weight:800">✏️ Redigér spørgsmålene til «'+fbaEsc(v.titel)+'»</summary>'+
            '<label style="display:block;font-size:.82rem;margin-top:10px;color:var(--graa)">Titel på målingen<input id="statTitel" value="'+fbaEsc(v.titel)+'" maxlength="120" style="width:100%;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:9px;padding:8px 12px;font-family:var(--font);margin-top:3px"></label>'+
            '<div id="statEdListe" style="display:grid;gap:8px;margin-top:12px"></div>'+
            '<div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:10px">'+
            '<button id="statAddSkala" style="border:1px dashed rgba(30,55,76,.3);background:#fff;border-radius:10px;padding:8px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">＋ Skala-spørgsmål (1-5)</button>'+
            '<button id="statAddTekst" style="border:1px dashed rgba(30,55,76,.3);background:#fff;border-radius:10px;padding:8px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">＋ Fritekst-spørgsmål</button></div>'+
            (v.antal?'<div style="color:var(--graa);font-size:.78rem;margin-top:8px">⚠️ Denne måling har allerede svar. Du kan fint rette formuleringer, men vær varsom med at fjerne/tilføje spørgsmål midt i en måling — overvej i stedet at åbne en ny.</div>':'')+
            '<button id="statGem" class="fba-send" style="border:0;border-radius:10px;padding:9px 16px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.85rem;margin-top:10px">💾 Gem ændringer</button></details>';
    }
    liste.innerHTML=html;
    if(v.aktiv)statEdTegn(v.spm||[]);
    statWire(j,sess);
}
function statEdRowHTML(s){
    const id=s&&s.id?s.id:"";const type=(s&&s.type==="tekst")?"tekst":"skala";const t=s&&s.t?s.t:"";
    return '<div class="stat-ed-row" data-id="'+fbaEsc(id)+'" style="display:flex;gap:8px;align-items:flex-start;background:#fff;border:1px solid rgba(30,55,76,.1);border-radius:10px;padding:8px">'+
        '<select class="stat-ed-type" style="border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:7px 8px;font-family:var(--font);font-size:.8rem;flex:none"><option value="skala"'+(type==="skala"?" selected":"")+'>Skala 1-5</option><option value="tekst"'+(type==="tekst"?" selected":"")+'>Fritekst</option></select>'+
        '<textarea class="stat-ed-txt" rows="1" maxlength="200" placeholder="Skriv spørgsmålet …" style="flex:1;box-sizing:border-box;border:1px solid rgba(30,55,76,.2);border-radius:8px;padding:7px 10px;font-family:var(--font);font-size:.86rem;resize:vertical;min-height:38px">'+fbaEsc(t)+'</textarea>'+
        '<button class="stat-ed-slet" title="Fjern spørgsmålet" style="border:0;background:rgba(231,0,76,.1);color:var(--roed);border-radius:8px;width:34px;height:34px;cursor:pointer;font-size:1rem;flex:none">🗑</button></div>';
}
function statEdTilfoej(box,s){const d=document.createElement("div");d.innerHTML=statEdRowHTML(s);const row=d.firstChild;box.appendChild(row);row.querySelector(".stat-ed-slet").onclick=()=>{row.remove();statEdRedigerer=true;};}
function statEdTegn(spm){const box=document.getElementById("statEdListe");if(!box)return;box.innerHTML="";spm.forEach(s=>statEdTilfoej(box,s));}
function statEdSaml(){const box=document.getElementById("statEdListe");if(!box)return[];const ud=[];
    box.querySelectorAll(".stat-ed-row").forEach(r=>{const t=r.querySelector(".stat-ed-txt").value.trim();if(!t)return;
        ud.push({id:r.getAttribute("data-id")||"",t:t,type:r.querySelector(".stat-ed-type").value==="tekst"?"tekst":"skala"});});
    return ud;}
function statWire(j,sess){
    const liste=document.getElementById("fbaListe");
    liste.querySelectorAll(".stat-vaelg").forEach(b=>{b.onclick=()=>{statValgtMid=b.getAttribute("data-mid");statTekstSort="ny";renderStat();};});
    const ny=document.getElementById("statNy");
    if(ny)ny.onclick=async function(){
        const titel=prompt("Titel på den nye måling:","Tilfredshedsundersøgelse");if(titel===null)return;
        let kopi="";
        if(statValgtMid&&confirm("Vil du kopiere spørgsmålene fra den måling, du kigger på nu, som udgangspunkt?\n\n(Tryk Annullér for at starte med standard-spørgsmål.)"))kopi=statValgtMid;
        if((j.aktivId)&&!confirm("Der er allerede en åben måling. Når du åbner en ny, LUKKES den nuværende automatisk (svarene bevares). Fortsæt?"))return;
        this.disabled=true;
        try{const r=await apiKald({a:"maalingny",token:sess.token,titel:titel.trim(),kopi:kopi});
            if(r&&r.ok){statValgtMid=r.mid;renderStat();}else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke åbne");}}
        catch(e){this.disabled=false;}
    };
    const luk=document.getElementById("statLuk");
    if(luk)luk.onclick=async function(){
        if(!confirm("Luk målingen? Borgerne kan ikke længere svare, men alle svar bevares og kan ses her bagefter."))return;
        this.disabled=true;
        try{const r=await apiKald({a:"maalingluk",token:sess.token,mid:statValgtMid});
            if(r&&r.ok)renderStat();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke lukke");}}
        catch(e){this.disabled=false;}
    };
    const sort=document.getElementById("statSort");
    if(sort)sort.onchange=()=>{statTekstSort=sort.value;renderStat();};
    const box=document.getElementById("statEdListe");
    const edBox=document.getElementById("statEdBox");
    if(edBox)edBox.addEventListener("toggle",()=>{if(edBox.open)statEdRedigerer=true;});
    const aS=document.getElementById("statAddSkala"),aT=document.getElementById("statAddTekst");
    if(aS)aS.onclick=()=>{statEdRedigerer=true;statEdTilfoej(box,{type:"skala",t:""});};
    if(aT)aT.onclick=()=>{statEdRedigerer=true;statEdTilfoej(box,{type:"tekst",t:""});};
    const gem=document.getElementById("statGem");
    if(gem)gem.onclick=async function(){
        const spm=statEdSaml();
        if(!spm.length){alert("Tilføj mindst ét spørgsmål.");return;}
        const titel=(document.getElementById("statTitel")||{}).value||"";
        this.disabled=true;this.textContent="Gemmer …";
        try{const r=await apiKald({a:"maalinggem",token:sess.token,mid:statValgtMid,titel:titel.trim(),spm:spm});
            if(r&&r.ok){statEdRedigerer=false;this.textContent="✔ Gemt";setTimeout(()=>renderStat(),700);}
            else{this.disabled=false;this.textContent="💾 Gem ændringer";alert((r&&r.fejl)||"Kunne ikke gemme");}}
        catch(e){this.disabled=false;this.textContent="💾 Gem ændringer";}
    };
}

/* ===================== 🌍 Fællesskabet — moderation (perm 'moderation') ===================== */
async function renderFsAdmin(){
    const liste=document.getElementById("fbaListe"),sess=fbaSess();
    if(!liste||!sess)return;
    liste.innerHTML='<p style="color:var(--graa)">⏳ Henter …</p>';
    let j=null;
    try{j=await apiKald({a:"fsadmin",token:sess.token});}catch(e){}
    if(!(j&&j.ok)){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
    const kat=j.kategorier||{};
    const scanBadge=x=>x.scan===1?'<span class="fba-side" style="background:rgba(231,0,76,.16);color:var(--roed)">⚠️ AI-flag: '+fbaEsc(x.scanGrund||"upassende indhold")+'</span>'
        :x.scan===-1?'<span class="fba-side" style="background:rgba(30,55,76,.08)">AI-scan utilgængelig</span>'
            :'<span class="fba-side" style="background:rgba(147,213,0,.18);color:#41630a">✓ AI-scan ok</span>';
    /* godkendelses-køen */
    let html='<div class="fba-kort" style="border-left:5px solid '+(j.afventer.length?'var(--roed)':'rgba(30,55,76,.15)')+'">'+
        '<b>⏳ Til godkendelse <span class="antal" style="background:rgba(231,0,76,.14);color:var(--roed)">'+j.afventer.length+'</span></b>'+
        '<p style="color:var(--graa);font-size:.8rem;margin:4px 0 0">Rene opslag publiceres automatisk — her lander KUN det, AI-scannet har markeret (eller ikke kunne tjekke). Du træffer afgørelsen.</p>'+
        (j.afventer.map(x=>{
            const erK=x.type==="kommentar",id=erK?x.kid:x.oid;
            return '<div style="margin-top:10px;padding:11px 14px;background:#fff;border-radius:12px;border:1px solid '+(x.scan===1?'rgba(231,0,76,.4)':'rgba(30,55,76,.08)')+'">'+
                '<div class="fba-top" style="margin-bottom:4px"><span class="fba-side">'+(erK?'💬 kommentar til «'+fbaEsc(x.opslagTitel||"")+'»':(kat[x.kat]||x.kat))+'</span>'+
                '<span class="fba-hvem" data-bku="'+fbaEsc(x.u)+'">'+fbaEsc(x.navn)+' ('+fbaEsc(x.u)+')</span>'+
                '<span style="color:var(--graa);font-size:.8rem">'+fbaDato(x.ts)+'</span>'+scanBadge(x)+'</div>'+
                (erK?'':'<b>'+fbaEsc(x.titel)+'</b>')+
                '<div style="font-size:.9rem;color:#333;white-space:pre-wrap;margin-top:3px">'+fbaEsc(x.txt)+'</div>'+
                '<div style="display:flex;gap:8px;margin-top:10px">'+
                '<button class="fba-send fsm" data-t="'+(erK?"kommentar":"opslag")+'" data-id="'+id+'" data-h="godkend" style="border:0;border-radius:10px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">✓ Godkend</button>'+
                '<button class="fba-done fsm" data-t="'+(erK?"kommentar":"opslag")+'" data-id="'+id+'" data-h="afvis" style="border:0;border-radius:10px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">✕ Afvis</button></div></div>';
        }).join("")||'<p style="color:var(--graa);margin:8px 0 0">Intet venter 🎉</p>')+'</div>';
    /* anmeldelser */
    html+='<div class="fba-kort"><b>🚩 Anmeldelser <span class="antal">'+j.anmeldt.length+'</span></b>'+
        (j.anmeldt.map(a2=>'<div class="fba-top" style="margin-top:8px;padding:9px 12px;background:#fff;border-radius:11px;border:1px solid rgba(231,0,76,.2)">'+
            '<b>«'+fbaEsc(a2.titel)+'»</b><span style="color:var(--graa);font-size:.83rem;font-style:italic">«'+fbaEsc(a2.grund||"")+'»</span>'+
            '<span style="color:var(--graa);font-size:.78rem">'+fbaDato(a2.ts)+'</span>'+
            '<button class="fba-done fs-anm-klar" data-id="'+a2.id+'" style="margin-left:auto">✓ Behandlet</button></div>').join("")||'<p style="color:var(--graa);margin:8px 0 0">Ingen åbne anmeldelser.</p>')+'</div>';
    /* HF-opslag */
    html+='<details class="fba-kort" style="border-left:5px solid var(--blaa)"><summary style="cursor:pointer;font-weight:800">📣 Skriv et officielt HF-opslag</summary>'+
        '<div style="display:grid;gap:10px;margin-top:10px">'+
        '<select id="fsHfKat" class="nya-felt">'+Object.keys(kat).map(k=>'<option value="'+k+'">'+kat[k]+'</option>').join("")+'</select>'+
        '<input id="fsHfTitel" class="nya-felt" maxlength="100" placeholder="Overskrift">'+
        '<textarea id="fsHfTxt" class="nya-felt" rows="4" maxlength="4000" placeholder="Opslaget publiceres med det samme, med HF-badge og dit visningsnavn."></textarea>'+
        '<button id="fsHfSend" class="fba-send" style="border:0;border-radius:11px;padding:11px;cursor:pointer;font-family:var(--font);font-weight:800;font-size:.9rem;justify-self:start">📣 Publicér HF-opslag</button></div></details>';
    /* aktive */
    html+='<details class="fba-kort"><summary style="cursor:pointer;font-weight:800">🌍 Aktive opslag — '+j.aktive.length+'</summary>'+
        (j.aktive.map(o=>'<div class="fba-top" style="margin-top:8px;padding:9px 12px;background:#fff;border-radius:11px;border:1px solid rgba(30,55,76,.07)">'+
            '<b>'+fbaEsc(o.titel)+'</b><span style="color:var(--graa);font-size:.82rem">'+fbaEsc(o.navn)+(o.hf?' · HF':'')+' · '+fbaDato(o.ts)+(o.laast?' · 🔒':'')+'</span>'+
            '<span style="margin-left:auto;display:flex;gap:6px">'+
            '<button class="fba-done fsm" data-t="opslag" data-id="'+o.oid+'" data-h="'+(o.laast?'aabn':'laas')+'">'+(o.laast?'🔓 Åbn':'🔒 Lås')+'</button>'+
            '<button class="fba-done fsm" data-t="opslag" data-id="'+o.oid+'" data-h="skjul">🙈 Skjul</button></span></div>').join("")||'<p style="color:var(--graa);margin:8px 0 0">Ingen endnu.</p>')+'</details>';
    /* skjulte & afviste — kan genåbnes */
    const skj=j.skjulte||[];
    html+='<details class="fba-kort"><summary style="cursor:pointer;font-weight:800">🙈 Skjulte &amp; afviste opslag — '+skj.length+'</summary>'+
        (skj.map(o=>'<div style="margin-top:8px;padding:10px 13px;background:#fff;border-radius:11px;border:1px solid rgba(30,55,76,.08)">'+
            '<div class="fba-top" style="margin-bottom:3px"><b>'+fbaEsc(o.titel)+'</b>'+
            '<span class="fba-side" style="'+(o.status==="afvist"?'background:rgba(231,0,76,.14);color:var(--roed)':'background:rgba(30,55,76,.1)')+'">'+(o.status==="afvist"?"afvist":"skjult")+'</span>'+
            '<span style="color:var(--graa);font-size:.82rem">'+fbaEsc(o.navn)+' · '+fbaDato(o.ts)+(o.modAf?' · af '+fbaEsc(visA(o.modAf)):'')+'</span></div>'+
            '<div style="font-size:.87rem;color:#333;white-space:pre-wrap">'+fbaEsc((o.txt||"").slice(0,300))+((o.txt||"").length>300?" …":"")+'</div>'+
            '<button class="fba-send fsm" data-t="opslag" data-id="'+o.oid+'" data-h="genaabn" style="margin-top:8px;border:0;border-radius:10px;padding:8px 15px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.83rem">↩ Vis igen i Fællesskabet</button></div>').join("")||'<p style="color:var(--graa);margin:8px 0 0">Ingen skjulte eller afviste.</p>')+'</details>';
    liste.innerHTML=html;
    liste.querySelectorAll(".fsm").forEach(b=>{
        b.onclick=async function(){
            const h=this.getAttribute("data-h");
            if(h==="afvis"&&!confirm("Afvis? Borgeren ser «ikke godkendt» på sit eget opslag."))return;
            if(h==="skjul"&&!confirm("Skjul opslaget fra Fællesskabet?"))return;
            this.disabled=true;
            try{const r=await apiKald({a:"fsmod",token:sess.token,type:this.getAttribute("data-t"),id:this.getAttribute("data-id"),handling:h});
                if(r&&r.ok)renderFsAdmin();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke gemme");}}
            catch(e){this.disabled=false;}
        };
    });
    liste.querySelectorAll(".fs-anm-klar").forEach(b=>{
        b.onclick=async function(){
            this.disabled=true;
            try{const r=await apiKald({a:"fsanmeldklar",token:sess.token,id:this.getAttribute("data-id")});
                if(r&&r.ok)renderFsAdmin();else this.disabled=false;}
            catch(e){this.disabled=false;}
        };
    });
    document.getElementById("fsHfSend").onclick=async function(){
        const titel=document.getElementById("fsHfTitel").value.trim(),txt=document.getElementById("fsHfTxt").value.trim();
        if(!titel||!txt){alert("Udfyld både overskrift og tekst.");return;}
        this.disabled=true;
        try{const r=await apiKald({a:"fshfopslag",token:sess.token,kat:document.getElementById("fsHfKat").value,titel:titel,txt:txt});
            if(r&&r.ok)renderFsAdmin();else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke publicere");}}
        catch(e){this.disabled=false;}
    };
}

/* ===================== 🤖 Chat-opsamling (hvad spørger folk om?) ===================== */
/* Kilde: data/chat-ai.jsonl via 'chattraade'. Beskeder grupperes i tråde
   (pause > 60 min = ny tråd), én linje pr. tråd, grupperet pr. bruger.
   Triage-status pr. tråd: ny → opdater/konsulent → arkiv, med kort log. */
let ctData=null,ctFane="ny",ctKunPas=true,ctNavne=null;
const ctAabneB=new Set(),ctAabneT=new Set();  /* husker åbne brugere/tråde over genrender */
const CT_STATUS=[["ny","📥 Nye"],["opdater","🛠 Til opdatering af chat"],["konsulent","👤 Til opfølgning af konsulent"],["arkiv","🗄 Arkiv"]];
const CT_NAVN={ny:"Nye",opdater:"Til opdatering af chat",konsulent:"Til opfølgning af konsulent",arkiv:"Arkiv"};
/* «måtte melde pas»-mærket: AI-fejl eller et svar der ligner et grænse-standardsvar */
const CT_PAS=/(ikke et sikkert svar|har jeg ikke .{0,25}svar|kan jeg ikke (se|svare|hjælpe)|kan jeg desværre ikke|har jeg desværre ikke|ved (jeg|vi) desværre ikke|uden for .{0,20}vidensbase|ikke noget sikkert om|del helst ikke cpr|ring med det samme til 71)/i;
function ctPas(m){return !!m.fejl||CT_PAS.test(String(m.svar||""));}
function ctNavn(u){
    if(!ctNavne){ctNavne={};try{adgangsBorgere(true).forEach(b=>{ctNavne[String(b.u).toLowerCase()]=b.n||"";});}catch(e){}}
    return ctNavne[String(u).toLowerCase()]||"";
}
function ctByg(){
    const pr={};
    (ctData.linjer||[]).forEach(e=>{if(!e||!e.sp)return;const u=String(e.u||"ukendt");(pr[u]=pr[u]||[]).push(e);});
    const traade=[];
    Object.keys(pr).forEach(u=>{
        let t=null;
        pr[u].slice().sort((a,b)=>(a.ts||0)-(b.ts||0)).forEach(m=>{
            if(!t||(m.ts||0)-t.slut>3600){t={id:"ct-"+u+"-"+(m.ts||0),u:u,start:m.ts||0,slut:m.ts||0,msgs:[]};traade.push(t);}
            t.msgs.push(m);t.slut=m.ts||0;
        });
    });
    traade.forEach(t=>{
        t.pas=t.msgs.some(ctPas);
        const tri=(ctData.triage||{})[t.id]||{};
        t.s=tri.s||"ny";t.log=tri.log||[];
    });
    return traade;
}
async function ctSaet(id,status,note){
    const sess=fbaSess();if(!sess)return;
    const j=await apiKald({a:"chattriage",token:sess.token,id:id,status:status,note:note||""});
    if(!(j&&j.ok)){alert((j&&j.fejl)||"Kunne ikke gemme");return;}
    ctData.triage=ctData.triage||{};ctData.triage[id]=j.triage;
    renderChatSamling();
}
function ctTraadHTML(t){
    const foerste=String(t.msgs[0].sp||"");
    const linje=fbaEsc(foerste.length>90?foerste.slice(0,90)+" …":foerste);
    const beskeder=t.msgs.map(m=>{
        const q='<div style="font-weight:700;margin-top:10px">🙋 '+fbaEsc(m.sp||"")+'</div>';
        const a=m.fejl
            ?'<div style="color:var(--roed);font-size:.88rem">⚠ AI-fejl: '+fbaEsc(m.fejl)+'</div>'
            :'<div style="color:#333;font-size:.88rem;white-space:pre-wrap">'+(ctPas(m)?'🚩 ':'')+fbaEsc(m.svar||"")+'</div>';
        return q+a;
    }).join("");
    const kn=[];
    if(t.s==="ny"){kn.push(["opdater","🛠 Til chat-opdatering"],["konsulent","👤 Til konsulent"],["arkiv","🗄 Arkivér"]);}
    else if(t.s==="opdater"){kn.push(["arkiv","✓ Behandlet → arkiv"],["konsulent","👤 Til konsulent"]);}
    else if(t.s==="konsulent"){kn.push(["arkiv","✓ Behandlet → arkiv"],["opdater","🛠 Til chat-opdatering"]);}
    else{kn.push(["ny","↩ Genåbn"]);}
    const knapper=kn.map(k=>'<button class="fba-done ct-kn" data-id="'+t.id+'" data-st="'+k[0]+'" style="border:0;border-radius:10px;padding:8px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">'+k[1]+'</button>').join(" ")+
        ' <button class="fba-done ct-viden" data-id="'+t.id+'" title="Send tråden som udkast til vidensbasen" style="border:0;border-radius:10px;padding:8px 13px;cursor:pointer;font-family:var(--font);font-weight:700;font-size:.82rem">📚 → vidensbase-udkast</button>';
    const log=(t.log||[]).slice(-6).map(l=>{
        const flyt=l.fra===l.til?"notat":CT_NAVN[l.fra]+" → "+CT_NAVN[l.til];
        return '<div style="color:var(--graa);font-size:.78rem">'+fbaDato(l.ts||0)+' · '+fbaEsc(l.af||"")+': '+fbaEsc(flyt)+(l.note?' — «'+fbaEsc(l.note)+'»':'')+'</div>';
    }).join("");
    return '<details class="ct-traad" data-id="'+t.id+'" '+(ctAabneT.has(t.id)?"open":"")+' style="margin:6px 0;background:#fff;border-radius:12px;border:1px solid rgba(30,55,76,.08);padding:8px 14px">'+
        '<summary style="cursor:pointer;font-size:.88rem">'+(t.pas?'🚩 ':'')+'<b>'+fbaDato(t.start)+'</b> · '+linje+' <span style="color:var(--graa)">💬 '+t.msgs.length+'</span></summary>'+
        beskeder+
        '<div style="display:flex;gap:8px;flex-wrap:wrap;margin:12px 0 6px">'+knapper+
        '<input class="ct-note" data-id="'+t.id+'" placeholder="Kort notat om hvad vi har gjort …" style="flex:1;min-width:180px;border:1px solid rgba(30,55,76,.18);border-radius:10px;padding:8px 11px;font-family:var(--font);font-size:.82rem">'+
        '<button class="ct-kn" data-id="'+t.id+'" data-st="notat" style="border:0;border-radius:10px;padding:8px 13px;cursor:pointer;background:rgba(60,180,229,.14);color:var(--moerk);font-family:var(--font);font-weight:700;font-size:.82rem">➕ Notat</button></div>'+
        (log?'<div style="border-top:1px dashed rgba(30,55,76,.12);padding-top:6px">'+log+'</div>':'')+
        '</details>';
}
async function renderChatSamling(){
    const liste=document.getElementById("fbaListe");
    const sess=fbaSess();if(!sess)return;
    if(!ctData){
        liste.innerHTML='<p style="color:var(--graa)">⏳ Henter chat-log …</p>';
        try{
            const j=await apiKald({a:"chattraade",token:sess.token});
            if(!(j&&j.ok)){liste.innerHTML='<p style="color:var(--roed)">'+fbaEsc((j&&j.fejl)||"Kunne ikke hente")+'</p>';return;}
            ctData=j;
        }catch(e){liste.innerHTML='<p style="color:var(--roed)">Kunne ikke hente — kræver serveren.</p>';return;}
        if(fbaG!=="chat")return;   /* admin nåede at skifte fane imens */
    }
    const alle=ctByg();
    const antal={};CT_STATUS.forEach(s=>{antal[s[0]]=alle.filter(t=>t.s===s[0]).length;});
    let traade=alle.filter(t=>t.s===ctFane);
    if(ctFane==="ny"&&ctKunPas)traade=traade.filter(t=>t.pas);
    /* gruppér pr. bruger, nyeste aktivitet øverst */
    const grp={};traade.forEach(t=>{(grp[t.u]=grp[t.u]||[]).push(t);});
    const brugere=Object.keys(grp).sort((a,b)=>Math.max(...grp[b].map(t=>t.slut))-Math.max(...grp[a].map(t=>t.slut)));
    liste.innerHTML=
        '<div class="fba-bar" style="margin-bottom:10px">'+
        CT_STATUS.map(s=>'<button class="ct-fane'+(s[0]===ctFane?' aktiv':'')+'" data-f="'+s[0]+'">'+s[1]+' <span style="opacity:.65">('+antal[s[0]]+')</span></button>').join("")+
        (ctFane==="ny"?'<label class="fba-vis"><input type="checkbox" id="ctKunPas" '+(ctKunPas?"checked":"")+'> Kun tråde hvor chatten måtte melde pas 🚩</label>':'')+
        '</div>'+
        (brugere.length?brugere.map(u=>{
            const g=grp[u].slice().sort((a,b)=>b.slut-a.slut);
            const navn=ctNavn(u);
            return '<details class="fba-kort ct-bruger" data-u="'+fbaEsc(u)+'" '+(ctAabneB.has(u)||brugere.length===1?"open":"")+'>'+
                '<summary style="cursor:pointer;font-weight:800">'+(navn?fbaEsc(navn)+' ('+fbaEsc(u)+')':fbaEsc(u))+' — '+g.length+' tråd'+(g.length===1?'':'e')+
                (g.some(t=>t.pas)?' <span style="color:var(--roed)">🚩</span>':'')+'</summary>'+
                g.map(ctTraadHTML).join("")+'</details>';
        }).join(""):'<p style="color:var(--graa)">Ingen tråde her'+(ctFane==="ny"&&ctKunPas?' — prøv at slå 🚩-filteret fra':'')+' 🎉</p>');
    /* lyttere */
    liste.querySelectorAll(".ct-fane").forEach(b=>{b.onclick=()=>{ctFane=b.getAttribute("data-f");renderChatSamling();};});
    const kp=document.getElementById("ctKunPas");if(kp)kp.onchange=()=>{ctKunPas=kp.checked;renderChatSamling();};
    liste.querySelectorAll(".ct-bruger").forEach(d=>{d.addEventListener("toggle",()=>{const u=d.getAttribute("data-u");if(d.open)ctAabneB.add(u);else ctAabneB.delete(u);});});
    liste.querySelectorAll(".ct-traad").forEach(d=>{d.addEventListener("toggle",()=>{const id=d.getAttribute("data-id");if(d.open)ctAabneT.add(id);else ctAabneT.delete(id);});});
    liste.querySelectorAll(".ct-viden").forEach(b=>{
        b.onclick=async function(){
            const t=alle.find(x=>x.id===this.getAttribute("data-id"));if(!t)return;
            const txt="FRA CHAT-OPSAMLINGEN ("+fbaDato(t.start)+"):\n"+
                t.msgs.map(m=>"Spørgsmål: "+(m.sp||"")+(m.svar?"\nChattens svar: "+m.svar:m.fejl?"\n(AI-fejl)":"")).join("\n---\n");
            this.disabled=true;
            try{const r=await apiKald({a:"videnudkast",token:fbaSess().token,txt:txt.slice(0,3000),ref:t.id});
                if(r&&r.ok){this.textContent="✔ sendt til vidensbasen";}
                else{this.disabled=false;alert((r&&r.fejl)||"Kunne ikke sende");}}
            catch(e){this.disabled=false;}
        };
    });
    liste.querySelectorAll(".ct-kn").forEach(b=>{
        b.onclick=async function(){
            const id=this.getAttribute("data-id"),st=this.getAttribute("data-st");
            let note="";
            const felt=liste.querySelector('.ct-note[data-id="'+id.replace(/"/g,'\\"')+'"]');
            if(felt)note=felt.value.trim();
            if(st==="notat"&&!note){alert("Skriv en kort note først.");return;}
            this.disabled=true;
            try{await ctSaet(id,st,note);}catch(e){this.disabled=false;alert("Kunne ikke gemme — tjek forbindelsen.");}
        };
    });
}

(function(){
    const bar=document.getElementById("fbaBar");
    if(!bar)return;
    bar.querySelectorAll("button[data-g]").forEach(b=>{
        b.onclick=()=>{fbaG=b.getAttribute("data-g");
            bar.querySelectorAll("button[data-g]").forEach(x=>x.classList.toggle("aktiv",x===b));
            renderFba();};
    });
    document.getElementById("fbaVisH").onchange=()=>renderFba();
    document.getElementById("fbaOpdater").onclick=()=>{fbaAdgang=null;fbaKons=null;fbaHub=null;ctData=null;ctNavne=null;lcaData=null;lcaAaben=null;
        renderLiveChat();   /* live-chat-panelet genindlæses altid */
        if(fbaG==="adgang")renderAdgang();else if(fbaG==="kons")renderKons();else if(fbaG==="chat")renderChatSamling();else hentFbAdmin();};
})();

/* ===================== Reveal-animationer ===================== */
var revObs=null; /* var: sætReveal kan kaldes af gate/efterLogin før denne linje er nået */
function sætReveal(){
    if(!("IntersectionObserver" in window)){document.querySelectorAll(".reveal").forEach(e=>e.classList.add("set"));return;}
    if(!revObs)revObs=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.add("set");revObs.unobserve(e.target);}}),{threshold:.08});
    document.querySelectorAll(".reveal:not(.set)").forEach(e=>revObs.observe(e));
}
sætReveal();
faqRender();     /* vis FAQ straks (standard) */
teksterHent();   /* ✏️ hent + anvend redigerbare borger-tekster (FAQ + tekstblokke) — opdaterer bagefter */

/* ===================== Chat (simpel hjælpe-bot — AI-chat kommer senere) ===================== */
(function(){
    const fab=document.getElementById("chatFab"),win=document.getElementById("chatWin"),luk=document.getElementById("chatLuk"),
        log=document.getElementById("cLog"),inp=document.getElementById("cInput"),send=document.getElementById("cSend"),
        mBar=document.getElementById("cMenneske"),mKnap=document.getElementById("cMenneskeKnap"),
        cStatus=document.getElementById("cStatus"),cTitel=document.getElementById("cTitel"),cAvatar=document.getElementById("cAvatar");
    const KB=[
        {k:["syg","sygemeld","sygdom","dækkes","dække","vikar","akut","nat"],
            s:"🤒 Melder en hjælper sig syg, registrerer du det i mitHF-BPA. Kan teamet ikke dække vagten, ringer du til vikarbooking på 71 74 70 25 — den er døgnåben, året rundt."},
        {k:["løn","frist","vagtplan","15","16","deadline","udbetal"],
            s:"💰 Lønperioden løber fra den 16. til den 15. i næste måned. Indtast vagter løbende — senest når perioden slutter den 15. Vi sender en SMS-påmindelse hver måned."},
        {k:["ansæt","ny hjælper","jobopslag","rekrutter","kandidat"],
            s:"👥 Bestil et jobopslag via formularen — så skriver vi opslaget og lægger det på hjemmesiden. Når du har valgt din kandidat, bruger du formularen «Ansættelse af ny hjælper». Begge finder du under Formularer."},
        {k:["skade","forsikring","bil","tingskade","selvrisiko"],
            s:"💥 Skader anmeldes med forsikringsformularerne under Formularer: «Tingskade» for dine ting og «Selvrisiko/bilskade» for din bil."},
        {k:["udland","rejse","ferie"],
            s:"✈️ Du kan som udgangspunkt tage hjælpere med til udlandet — der gælder særlige regler. Med §96 kan du søge dækning af hjælpernes udgifter til én årlig rejse (op til 14 dage). Brug udlandsformularen og tal med din konsulent i god tid."},
        {k:["bevilling","kommune","afgørelse","budget","timer","genvurder"],
            s:"📄 Ny bevilling, afgørelse eller budget fra kommunen? Indsend den via bevillingsformularen under Formularer, så opdaterer vi din ordning med det samme. Vil kommunen ændre din bevilling, står din konsulent og vores BPA Rådgivning klar."},
        {k:["login","kode","adgang","mithf","temponizer","logge ind"],
            s:"🔑 Login til mitHF-BPA: tjek først din spammappe. Mangler det stadig, så kontakt din konsulent eller ring 70 777 333. Vil du øve dig først, så brug læringshubben her i universet."},
        {k:["lær","øv","demo","hub","kursus","undervis","video"],
            s:"🎓 I læringshubben øver du dig risikofrit med guidede demoer og øvelser — klik på «Læringshub» på forsiden. Arbejdslederkurset er desuden et frivilligt tilbud: 1:1, 4–6 timer."},
        {k:["opsig","fyr","konflikt"],
            s:"✂️ Skal en hjælper opsiges, kontakter du din konsulent. Vi håndterer det formelle og hjælper gerne med konflikthåndtering."},
        {k:["arrangement","fest","sommerfest","event","nyhedsbrev"],
            s:"🎪 Vi holder sommerfest, museumsrundvisninger, fredagsbarer m.m. — se «Nyheder & arrangementer». HF-Ledernyhedsbrevet lander i din indbakke cirka hver anden måned."},
        {k:["klage","utilfreds","ris","ros","fejl","idé"],
            s:"😞 Er vi ikke levet op til dine forventninger, vil vi gerne høre det: brug klageformularen. Fejl og idéer til IT-systemet sendes med «Ris & ros til IT-platformen» — begge under Formularer."},
        {k:["sikker","cpr","følsom","krypter","helbred"],
            s:"🔒 Følsomme oplysninger (CPR, helbred m.m.) sendes altid via sikkermail@handicapformidlingen.dk eller formularen «Send filer sikkert» — aldrig pr. almindelig mail."},
        {k:["kontakt","telefon","nummer","ring","mail","adresse","kontor","åbnings","besked","support","skriv"],
            s:"Der er flere måder at nå os på 💬\n\n• Nemmest: skriv til os under «Beskeder & support» her i universet — du får svar samme sted og kan vedhæfte billeder/filer.\n• Ring: 70 777 333 (hverdage 9–15) · SMS 5454 6521.\n• Akut/vikarbooking: 71 74 70 25 (døgnåben, året rundt).\n• Mail: info@handicapformidlingen.dk. Følsomt: sikkermail@handicapformidlingen.dk.\n\nDu kan også bede om at «tale med et menneske» her i chatten i åbningstiden."},
        {k:["koster","pris","betal","gratis"],
            s:"💙 Det koster dig ikke noget — al service er omkostningsfri for dig som borger, og hjælpernes løn følger din bevilling krone for krone."},
        {k:["ledsagekort"],
            s:"🪪 Du kan få et ledsagekort til dine hjælpere, betalt af Handicapformidlingen — brug formularen «Ledsagekort» under Formularer."},
        {k:["uth","utilsigtet","hændelse"],
            s:"⚠️ Utilsigtede hændelser rapporteres med UTH-formularen under Formularer."}
    ];
    const FALLBACK="Det har jeg ikke et sikkert svar på 🙈 Prøv at omformulere — eller ring til os på 70 777 333 (hverdage 9–15). Ved akut behov: 71 74 70 25, døgnåbent.";
    const chatHist=[];   /* samtalehistorik → AI'en kan forstå opfølgende spørgsmål */
    function tidStr(ts){const d=ts?new Date(ts*1000):new Date(),p=n=>(n<10?"0":"")+n;return p(d.getHours())+":"+p(d.getMinutes());}
    function boble(tekst,type,ts){   /* type: mig | bot | hf (medarbejder) | sys */
        const d=document.createElement("div");d.className="c-b "+type;d.textContent=tekst;
        log.appendChild(d);
        if(type!=="sys"){const t=document.createElement("span");t.className="c-tid";t.textContent=tidStr(ts);log.appendChild(t);}
        log.scrollTop=log.scrollHeight;
        return d;
    }
    function botSvar(tekst){boble(tekst,"bot");chatHist.push({r:"hf",t:tekst});}
    /* HTML-boble til rige kort (fx konsulent-kort m. billede + klikbare links) — indhold kommer fra vores egen server og escapes felt-for-felt */
    function botKort(html){
        const d=document.createElement("div");d.className="c-b bot";d.innerHTML=html;log.appendChild(d);
        const t=document.createElement("span");t.className="c-tid";t.textContent=tidStr();log.appendChild(t);
        log.scrollTop=log.scrollHeight;chatHist.push({r:"hf",t:"(viste dit konsulent-kort)"});
    }
    async function visMinKonsulent(){   /* borgerens EGEN konsulent — direkte fra profil, ikke via AI */
        visSkriv();
        let u="";try{u=(localStorage.getItem("bpa_user")||window.BPA_USER||"").trim().toLowerCase();}catch(e){u=(window.BPA_USER||"").toString().toLowerCase();}
        if(!u){fjernSkriv();botSvar("Log ind i universet, så kan jeg vise dig din faste konsulent. Ellers er du altid velkommen til at ringe på 70 777 333 (hverdage 9–15) 🤝");return true;}
        try{
            const j=await apiKald({a:"minkons",u:u});
            fjernSkriv();
            if(j&&j.ok&&j.kons&&j.kons.navn){
                const k=j.kons,pentTlf=(k.tlf||"").replace(/^(\d{2})(\d{3})(\d{3})$/,"$1 $2 $3");
                botKort('<div style="font-weight:800;color:var(--graa);font-size:.8rem;margin-bottom:4px">🤝 Din faste konsulent</div>'+
                    (k.foto?'<img src="'+fbaEsc(k.foto)+'" alt="'+fbaEsc(k.navn)+'" onerror="this.remove()" style="width:66px;height:66px;border-radius:50%;object-fit:cover;margin:2px 0 6px">':'')+
                    '<div style="font-weight:800;font-size:1.02rem">'+fbaEsc(k.navn)+'</div>'+
                    '<div style="color:var(--graa);font-size:.85rem;margin-bottom:7px">'+fbaEsc(k.titel||"Konsulent")+'</div>'+
                    (k.tlf?'<div style="margin:2px 0">📞 <a href="tel:'+fbaEsc(k.tlf)+'">'+fbaEsc(pentTlf)+'</a></div>':'')+
                    '<div style="margin:2px 0">✉️ <a href="#/support" onclick="window.__supNyStart=1">Skriv til din konsulent i beskedsystemet</a></div>'+
                    '<div style="color:var(--graa);font-size:.82rem;margin-top:7px">Din konsulent er altid din primære indgang. Kan du ikke få fat i vedkommende, så ring på 70 777 333.</div>');
            } else {
                botSvar("Jeg kan ikke se en fast konsulent på din profil endnu. Ring til os på 70 777 333 (hverdage 9–15), så finder vi ud af, hvem der er din konsulent 🤝");
            }
        }catch(e){fjernSkriv();botSvar("Jeg kunne ikke hente din konsulent lige nu. Prøv igen om lidt, eller ring på 70 777 333.");}
        return true;
    }
    /* animeret «skriver …»-indikator (tre hoppende prikker — som de store chatsystemer) */
    let skrivEl=null;
    function visSkriv(){if(skrivEl)return;skrivEl=document.createElement("div");skrivEl.className="c-skriv";
        skrivEl.innerHTML="<i></i><i></i><i></i>";log.appendChild(skrivEl);log.scrollTop=log.scrollHeight;}
    function fjernSkriv(){if(skrivEl){skrivEl.remove();skrivEl=null;}}
    async function svar(q){
        const n=q.toLowerCase();
        let best=null,score=0;
        for(const e of KB){let s=0;for(const k of e.k)if(n.includes(k))s++;if(s>score){score=s;best=e;}}
        /* «Hvem er min konsulent?» → svar direkte fra borgerens EGEN profil (ikke via AI, da det er persondata) */
        if(/(konsulent|kontaktperson|r[åa]dgiver|sagsbehandler)/.test(n) && /\b(min|mit|mig|jeg)\b/.test(n)){
            await visMinKonsulent();return;
        }
        /* AI-FØRST: spørg ALTID AI-hjælperen (hf-chat.php), så borgeren får et rigtigt, uddybende svar.
       Regel-bottens «best» bruges kun som nødplan længere nede, hvis AI'en er utilgængelig/lukket. */
        visSkriv();
        let adm=null;try{adm=JSON.parse(sessionStorage.getItem("bpa_adm")||"null");}catch(e){}
        try{
            const r=await fetch("hf-chat.php",{method:"POST",body:JSON.stringify({
                    sp:q,u:window.BPA_USER||"",admtoken:(adm&&adm.token)||"",hist:chatHist.slice(-6)
                })});
            const j=await r.json();
            fjernSkriv();
            if(j&&j.ok&&j.svar){botSvar(j.svar);return;}
        }catch(e){fjernSkriv();}
        botSvar(best?best.s:FALLBACK);   /* AI utilgængelig/test-lukket → regel-bottens bedste bud */
    }

    /* ===== menneske-chat (live-chat med en medarbejder) ===== */
    const lc={cid:null,mode:false,timer:null,efter:0,vist:new Set(),skrivTs:0,admNavn:"",statusTimer:null};
    function lcUI(){
        if(lc.mode){
            cAvatar.textContent="💁";
            cTitel.textContent=lc.admNavn?lc.admNavn+" · Handicapformidlingen":"Medarbejder-chat";
            cStatus.textContent=lc.admNavn?"💬 Du chatter med en rigtig medarbejder":"⏳ Du er i kø — et øjeblik …";
            mKnap.textContent="✕ Afslut chatten med medarbejderen";mBar.hidden=false;
            inp.placeholder="Skriv til medarbejderen …";
        }else{
            cAvatar.textContent="🤖";cTitel.textContent="HF-hjælperen";
            cStatus.textContent="Svarer med det samme · døgnet rundt";
            mKnap.textContent="👤 Tal med en medarbejder i stedet";
            inp.placeholder="Skriv dit spørgsmål …";
        }
    }
    async function lcStatusTjek(){
        if(lc.mode)return;
        if(!window.BPA_USER||window.BPA_USER==="faelles"){mBar.hidden=true;return;}
        try{
            const j=await apiKald({a:"lcstatus",u:window.BPA_USER});
            if(!(j&&j.ok))return;
            if(j.samtale){lc.admNavn=j.samtale.status==="aktiv"?j.samtale.adm:"";lcGenoptag(j.samtale.cid);return;}
            mBar.hidden=!j.aaben;
        }catch(e){}
    }
    function lcGenoptag(cid){lc.cid=cid;lc.mode=true;lc.efter=0;lc.vist.clear();lcUI();lcPollStart();}
    async function lcStart(){
        mKnap.disabled=true;
        try{
            const j=await apiKald({a:"lcstart",u:window.BPA_USER,n:window.BPA_NAME||""});
            mKnap.disabled=false;
            if(!(j&&j.ok)){boble((j&&j.fejl)||"Chatten med medarbejdere er lukket lige nu.","sys");mBar.hidden=true;return;}
            lc.cid=j.cid;lc.mode=true;lc.efter=0;lc.vist.clear();lc.admNavn="";
            lcUI();
            boble("Du er stillet i kø til en medarbejder ⏳ Skriv gerne dit spørgsmål allerede nu, så ligger det klar.","sys");
            lcPollStart();
        }catch(e){mKnap.disabled=false;boble("Kunne ikke starte chatten — tjek forbindelsen.","sys");}
    }
    function lcSlutLokal(){
        lc.mode=false;lc.cid=null;lc.admNavn="";
        if(lc.timer){clearInterval(lc.timer);lc.timer=null;}
        fjernSkriv();lcUI();
        boble("Du chatter nu igen med HF-hjælperen 🤖 — spørg løs.","sys");
        lcStatusTjek();
    }
    function lcPollStart(){if(lc.timer)clearInterval(lc.timer);lcPoll();lc.timer=setInterval(lcPoll,3500);}
    async function lcPoll(){
        if(!lc.mode)return;
        const genindlaes=lc.efter===0;   /* første hentning efter reload → vis også egne beskeder */
        try{
            const j=await apiKald({a:"lchent",u:window.BPA_USER,cid:lc.cid,efter:lc.efter});
            if(!(j&&j.ok))return;
            if(j.status==="aktiv"&&j.adm&&j.adm!==lc.admNavn){lc.admNavn=j.adm;lcUI();}
            if(j.status==="venter"&&lc.admNavn){lc.admNavn="";lcUI();}   /* medarbejderen gav slip → i kø igen */
            (j.beskeder||[]).forEach(b=>{
                lc.efter=Math.max(lc.efter,b.ts||0);
                const nkey=(b.ts||0)+"|"+b.fra+"|"+(b.txt||"");
                if(lc.vist.has(nkey))return;lc.vist.add(nkey);
                if(b.fra==="borger"){if(genindlaes)boble(b.txt,"mig",b.ts);return;}   /* egne vises straks ved afsendelse */
                fjernSkriv();
                boble(b.txt,b.fra==="system"?"sys":"hf",b.ts);
            });
            if(j.status==="arkiv"){lcSlutLokal();return;}
            if(j.skriver)visSkriv();else if(skrivEl&&lc.mode)fjernSkriv();
        }catch(e){}
    }
    mKnap.onclick=async ()=>{
        if(!lc.mode){lcStart();return;}
        if(!confirm("Afslut chatten med medarbejderen?"))return;
        try{await apiKald({a:"lcslut",u:window.BPA_USER,cid:lc.cid});}catch(e){}
        lcSlutLokal();
    };

    function sendMsg(){
        const v=inp.value.trim();if(!v)return;
        if(lc.mode){
            boble(v,"mig");inp.value="";inp.style.height="auto";
            lc.vist.add("egen|"+v);   /* markér som vist — serveren replayer den kun ved genindlæsning */
            apiKald({a:"lcsend",u:window.BPA_USER,cid:lc.cid,txt:v}).then(j=>{
                if(!(j&&j.ok))boble((j&&j.fejl)||"Beskeden kom ikke frem — prøv igen.","sys");
            }).catch(()=>boble("Beskeden kom ikke frem — tjek forbindelsen.","sys"));
            return;
        }
        boble(v,"mig");chatHist.push({r:"borger",t:v});inp.value="";inp.style.height="auto";svar(v);
        apiSend({a:"log",t:"chat",u:window.BPA_USER||"",n:window.BPA_NAME||"",q:v});
    }
    /* skrive-indikator den anden vej: fortæl serveren at borgeren taster (aldrig indholdet) */
    inp.addEventListener("input",()=>{
        if(!lc.mode||!inp.value.trim())return;
        const nu=Date.now();
        if(nu-lc.skrivTs>4000){lc.skrivTs=nu;apiSend({a:"lcskriv",u:window.BPA_USER,cid:lc.cid});}
    });

    let åbnet=false;
    fab.onclick=()=>{
        win.classList.toggle("vis");
        if(win.classList.contains("vis")){
            lcStatusTjek();
            if(!lc.statusTimer)lc.statusTimer=setInterval(()=>{if(win.classList.contains("vis"))lcStatusTjek();},60000);
            if(!åbnet){
                åbnet=true;
                const navn=(window.BPA_NAME||"").split(" ")[0];
                boble("Hej"+(navn?" "+navn:"")+" 👋 Jeg er HF-hjælperen — spørg bare! Jeg ved en masse om din BPA-ordning: løn, vagtplan, sygemelding, ferie, formularer, kontakt og meget mere. Hvad kan jeg hjælpe dig med?","bot");
                try{var _tip=["Du kan se din bevilling og dine resterende timer i mitHF-BPA under Vagtplanlægning.","Meld en hjælpers sygdom med det samme i mitHF-BPA — kan teamet ikke dække vagten, så ring til vikarbooking på 71 74 70 25 (døgnåbent).","Under «Beskeder & support» kan du skrive direkte til os og få svar samme sted — du kan endda vedhæfte et billede.","Prøv læringshubben — korte demoer i alt fra «opret en vagt» til «udrul vagtplan».","Du kan lave dine egne huskesedler under «Mine noter» — kun du kan se dem.","Spørg mig om din faste konsulent, så viser jeg dig navn, telefon og mail.","Lønperioden går fra den 16. til den 15. — godkend dine vagter i god tid.","Mangler du en hjælper? Bestil et jobopslag via formularen, så skriver vi opslaget for dig.","Skal dine hjælpere med til udlandet? Læs reglerne og giv din konsulent besked i god tid.","Du finder alle selvbetjenings-formularer samlet under «Formularer» i universet."];boble("💡 Dagens tip: "+_tip[Math.floor(Date.now()/864e5)%_tip.length],"bot");}catch(e){}
                setTimeout(()=>{try{inp.focus();}catch(e){}},200);
            }
        }
    };
    luk.onclick=()=>win.classList.remove("vis");
    send.onclick=sendMsg;
    inp.addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();sendMsg();}});
    inp.addEventListener("input",()=>hfGrow(inp,120));
})();
