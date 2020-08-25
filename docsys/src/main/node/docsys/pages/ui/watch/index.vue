<template>
  <v-app>
    <main-app-bar>NoSQLBench - Execution Status</main-app-bar>

    <!--
      {
        "summary": {
          "total_bytes": 24,
          "total_files": 1,
          "last_change": 1597293397043,
          "last_file_changed": "README.md"
        },
        "name": "default",
        "modified": 1597293397043,
        "changed": "README.md (1H17M27S ago)"
      }
    ]
    -->

    <v-main justify-start align-start class="d-inline-block pa-4 ma-10">
      <div class="row no-gutters">
        <v-card min-width="300" max-width="300" max-height="400" raised elevation="5"
                v-for="(invocation,w) in invocations" :key="w"
                class="pa-4 ma-4" :loading="is_loading(invocation)">
          <v-row :title="JSON.stringify(invocation)">
            <v-card-title title="running scenario name">{{ invocation.scenario_name }}</v-card-title>
<!--            <v-icon v-if="workspace === cardspace.name">mdi-check-bold</v-icon>-->
          </v-row>
          <v-card-subtitle title="title">sdf</v-card-subtitle>
          <v-card-subtitle title="started at">{{invocation.started_at}}</v-card-subtitle>


<!--          <v-card-subtitle title="last change">{{ abbrev(invocation.summary.last_changed_filename) }}</v-card-subtitle>-->
<!--          <v-divider></v-divider>-->
<!--          <v-list align-start>-->
<!--            <v-simple-table>-->
<!--              <tbody>-->
<!--              <tr>-->
<!--                <td>Bytes</td>-->
<!--                <td>{{ invocation.summary.total_bytes }}</td>-->
<!--              </tr>-->
<!--              <tr>-->
<!--                <td>Files</td>-->
<!--                <td>{{ invocation.summary.total_files }}</td>-->
<!--              </tr>-->
<!--              </tbody>-->
<!--            </v-simple-table>-->
<!--            <v-divider></v-divider>-->

<!--            <v-list-item>-->
<!--              <v-btn title="view details of workspace">-->
<!--                <v-icon>mdi-magnify</v-icon>-->
<!--              </v-btn>-->

<!--              &lt;!&ndash;              <v-btn title="use this workspace">&ndash;&gt;-->
<!--              &lt;!&ndash;                <v-icon>mdi-play</v-icon>&ndash;&gt;-->
<!--              &lt;!&ndash;              </v-btn>&ndash;&gt;-->

<!--              <v-spacer></v-spacer>-->

<!--              <v-btn title="download zipped workspace">-->
<!--                <v-icon>mdi-folder-download</v-icon>-->
<!--              </v-btn>-->

<!--              <v-spacer></v-spacer>-->

<!--              <v-btn title="purge workspace">-->
<!--                <v-icon @click="purgeWorkspace(cardspace.name)">mdi-trash-can</v-icon>-->
<!--              </v-btn>-->

<!--            </v-list-item>-->
<!--          </v-list>-->
        </v-card>
      </div>
    </v-main>

  </v-app>
</template>

<script>

import WorkspaceSelector from "@/components/WorkspaceSelector";
import {mapActions, mapGetters, mapMutations} from "vuex";
import AppSelector from "@/components/AppSelector";
import MainAppBar from "@/components/MainAppBar";

export default {
  name: "workspaces.vue",
  components: {
    MainAppBar,
    AppSelector,
    WorkspaceSelector
  },
  data(context) {
    let data = {
      // invocations: [
      //   {
      //     "scenario_name": "mytestscenario",
      //     "progress": [
      //       {
      //         "name": "/tmp/nosqlbench/mytestscenario3888869514662003808file1.yaml",
      //         "state": "Running",
      //         "details": "min=0 cycle=1692 max=10000000",
      //         "completed": 1.692E-4
      //       }
      //     ],
      //     "started_at": 1597749369800,
      //     "ended_at": -1,
      //     "activity_states": [
      //       {
      //         "completion": "1.692E-4",
      //         "name": "/tmp/nosqlbench/mytestscenario3888869514662003808file1.yaml",
      //         "state": "Running"
      //       }
      //     ]
      //   }
      // ]
    };
    return data;
  },
  computed: {
    invocations: {
      get() {
        return this.$store.getters["invocations/getInvocations"]
      }
    }
  },
  methods: {
    is_loading(invocation) {
      let found = invocation.progress.find(p => {p.state==='Running'});
      return !!found;
    },
    abbrev(name) {
      return name;
    }
  },
  created() {
    console.log("created component...");
    this.$store.dispatch("invocations/loadInvocations", "watch panel load");
  }
}

</script>

<style>
</style>
