<template>
  <v-container>

    <!-- OpenAPI Selection -->
    <v-row>
      <v-select
          v-if="mode==='selecting'"
          label="Use OpenAPI YAML"
          v-model="yamloptions"
          :items="yamlfiles_in_workspace"
      >
        <template v-slot:append-item>
          <v-list-item>
            <v-btn @click="mode='importing'">import</v-btn>
          </v-list-item>
        </template>
      </v-select>
    </v-row>

    <div v-if="mode==='importing'">

      <v-row>
        <v-text-field
            outlined
            label="OpenAPI YAML URL"
            @input="nameImport()"
            v-model="import_url"></v-text-field>

      </v-row>
      <v-row v-if="import_as">
        <v-text-field outlined
                      label="import as name"
                      v-model="import_as"></v-text-field>
      </v-row>
      <v-row>

        <v-btn
            @click="loadExample1()"
            v-if="!this.import_url"
            title="load https://gist.githubusercontent.com/jshook/529e1b3f80e6283459c55ae56255bbc5/raw/c0d2ac9853099b57ca6d2209661f332a3953ab02/stargate.yaml"
        >Load stargate.yaml
        </v-btn>

        <v-btn
            v-if="this.import_url && this.import_url.match('^http')"
            @click="importToWorkspace()"
        >Save to Workspace
        </v-btn>

      </v-row>
    </div>
  </v-container>
</template>

<script>
export default {
  name: "openapi",
  data() {
    return {
      openapifiles: [],
      mode: 'selecting',
      import_url: null,
      yamloptions: null,
      import_as: null,
      example_url: "https://gist.githubusercontent.com/jshook/529e1b3f80e6283459c55ae56255bbc5/raw/c0d2ac9853099b57ca6d2209661f332a3953ab02/stargate.yaml"
    }
  },
  computed: {
    yamlfiles_in_workspace: {
      get() {
        let wsfiles = this.$store.getters["workspaces/getMatchingFiles"]
      }
    },
    workspace: function () {
      return this.$store.getters["workspaces/getWorkspace"]
    }
  },
  methods: {
    refreshOpenApiFiles() {
      let wsfiles = this.$store.dispatch("workspaces/listWorkspaceFiles",
          {
            wsname: this.workspace,
            contains: '^openapi: 3.*'
          })
      console.log("wsfiles:" + JSON.stringify(wsfiles,null,2))
      // this.openapifiles=wsfiles.ls.map(f => f.name)
    },
    loadExample1() {
      this.import_url = this.example_url
      this.nameImport();
    },
    nameImport() {
      let parts = this.import_url.split("/");
      this.import_as = parts[parts.length - 1]
    },
    importToWorkspace() {
      this.$store.dispatch("workspaces/importUrlToWorkspace",
          {
            workspace: this.workspace,
            import_url: this.import_url,
            import_as: this.import_as
          }
      )
      this.refreshOpenApiFiles()
      this.mode = 'selecting'
    }
  },
  mounted() {
    this.refreshOpenApiFiles()
  }
}
</script>

<style scoped>

</style>
