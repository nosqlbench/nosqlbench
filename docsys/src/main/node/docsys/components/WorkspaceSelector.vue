<template>
<!--  <v-container fluid class="d-flex pa-3">-->
    <v-row>
      <v-col cols="12">
        <v-text-field dense
                      full-width
                      label="Name of new workspace"
                      v-if="mode==='adding'"
                      v-model="new_workspace"
                      ref="new_workspace_input"
                      hint="workspace name"
                      @blur="initializeWorkspace(new_workspace)"
                      @keydown.enter="initializeWorkspace(new_workspace)"
                      @keydown.esc="cancelWorkspace()"
        ></v-text-field>
        <!--    label="workspace"-->
        <v-select dense outlined
                  full-width
                  hide-details="true"
                  hint="current workspace"
                  v-if="mode==='showing'"
                  v-model="workspace"
                  :items="workspaces"
                  item-text="name"
                  item-value="name"
                  prepend-inner-icon="mdi-folder"
                  title="active workspace"
        >
          <template v-slot:append-item>
            <v-list-item>
              <v-btn link @click="addWorkspace()">+ Add Workspace</v-btn>
              <v-spacer></v-spacer>
              <v-btn to="/ui/workspaces">Manage</v-btn>
            </v-list-item>
          </template>
        </v-select>
      </v-col>
    </v-row>
<!--  </v-container>-->
</template>
<script>

export default {
  name: 'workspace-selector',
  data() {
    let mode = "showing";
    let new_workspace = "";
    return {mode, new_workspace}
  },
  computed: {
    workspace: {
      get() {
        return this.$store.getters["workspaces/getWorkspace"]
      },
      set(val) {
        this.$store.dispatch("workspaces/setWorkspace", val)
      }
    },
    workspaces: {
      get() {
        return this.$store.getters["workspaces/getWorkspaces"]
      },
      set(val) {
        this.$store.dispatch("workspaces/setWorkspaces", val)
      }
    }
  },
  methods: {
    cancelWorkspace: function () {
      this.mode = "showing";
      this.new_workspace = "";
    },
    addWorkspace: function () {
      this.mode = "adding";
      // this.$refs.new_workspace_input.focus();
      this.$nextTick(() => {
        this.$refs.new_workspace_input.focus();
      });
    },
    initializeWorkspace: function ({$store}) {
      // console.log("commit:" + JSON.stringify(this.new_workspace));
      this.$store.dispatch("workspaces/activateWorkspace", this.new_workspace);
      this.new_workspace = "";
      this.mode = "showing";
    }
  },
  created() {
    // console.log("created component...");
    this.$store.dispatch('workspaces/initWorkspaces', "selector load");
  }
}
</script>
<style>
</style>
