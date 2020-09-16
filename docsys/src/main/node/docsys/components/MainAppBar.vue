<template>
  <v-app-bar app fluid>
    <!--    <v-app-bar app dark fluid dense flat>-->
    <v-toolbar-title><slot></slot></v-toolbar-title>
    <v-spacer></v-spacer>
    <v-toolbar-items>
      <app-selector></app-selector>
      <workspace-selector></workspace-selector>
    </v-toolbar-items>
  </v-app-bar>

</template>

<script>
import AppSelector from "@/components/AppSelector";
import WorkspaceSelector from "@/components/WorkspaceSelector";

export default {
  name: "MainAppBar",
  components: {WorkspaceSelector, AppSelector}
}
</script>

<style scoped>
</style>
