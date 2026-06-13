import { createApp } from "vue";
import './assets/css/styles.css'
import App from "./App.vue";
import { createPinia } from "pinia";
import router from "./router";
import vuetify from "./plugins/vuetify";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);

app.use(router);

app.use(vuetify);

router.isReady().then(() => {
  app.mount("#app");
});
