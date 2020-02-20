<template>
  <v-navigation-drawer v-model="drawer" :permanent="lockmenu" @transitionend="setDrawer" app class="secondary">

    <div class="menu">
<!--      active_category: {{active_category}} active_topic: {{active_topic}}-->

      <!-- Use active_category and active_topic to select inactive -->

      <v-list dense>
        <v-list-item
          v-if="$vuetify.breakpoint.mdAndDown"
        >
          <v-list-item-action>
            <v-switch flat inset dark color="accent" v-model="lockmenu" label="keep open" @change="setLockMenu"></v-switch>
          </v-list-item-action>
        </v-list-item>

        <v-list-group link v-for="(category,c) in categories" :key="c"
                      :value="active_category === category.category">

          <template v-slot:activator>
            <v-list-item-content>
<!--              <router-link :to="{ name: 'docs-slug', params: {lockmenu:lockmenu}}">-->
                <router-link :to="{path: category.category+'.html'}">
                <v-list-item-title>
                  {{category.categoryName}}
                </v-list-item-title>
              </router-link>
            </v-list-item-content>
          </template>

          <v-list-item v-for="(doc, i) in category.docs" :key="i" link :to="doc.filename">
<!--            <router-link :to="{ name: 'docs-slug', params: {lockmenu:lockmenu}}">-->
              <router-link :to="{ path: doc.filename + '.html'}">
              <v-list-item-title>{{doc.attributes.title}}</v-list-item-title>
            </router-link>
          </v-list-item>

        </v-list-group>
      </v-list>

    </div>
  </v-navigation-drawer>
</template>
<script>
    export default {
        name: 'DocsMenu',
        props: {
            categories: Array,
            active_category: String,
            active_topic: String
        },
        data(context) {
            let lockmenu = this.$store.state.docs.isDrawerPinned;
            if (lockmenu == null){
              lockmenu = false
            }
            console.log("data says context is: " + context);
            // console.log("g says" + this.$getters.docs.drawerState);
            return {
                lockmenu: lockmenu,
                drawer: null
            }
        },
        methods: {
            setLockMenu() {
                this.$store.commit('docs/setMenuLock', this.lockmenu);
            },
            setDrawer() {
                this.$store.commit('docs/setDrawer', this.drawer);
            }
        },
        created() {
            this.setDrawer();
            this.$store.subscribe((mutation, state) => {
                console.log("mutation type " + mutation.type);
                if (mutation.type === 'docs/toggleDrawerState') {
                    this.drawer=this.$store.state.docs.isDrawerOpen;
                } else if (mutation.type === 'docs/setMenuLock') {
                    this.lockmenu=this.$store.state.docs.isDrawerPinned;
                }
            });
        }
        // computed: {
        //     drawerState() {
        //         console.log("getting drawerState...");
        //         return this.$store.getters.drawerState;
        //     }
        // },
    }
</script>
