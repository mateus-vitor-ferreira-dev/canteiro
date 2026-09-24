import { mount } from "svelte";
import App from "./App.svelte";
import "./estilos/global.css";

const alvo = document.getElementById("app");
if (!alvo) {
    throw new Error("Elemento #app não encontrado no index.html.");
}

export default mount(App, { target: alvo });
