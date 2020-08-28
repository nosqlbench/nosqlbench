<template>
  <v-app>
  <main-app-bar>NoSQLBench - Workload Builder</main-app-bar>

    <v-layout>

      <v-main>
        <v-container fluid>
          <v-layout row>
            <v-flex>
              <v-card>
                <v-card-title>
                  Workload details
                </v-card-title>
                <v-col
                    cols="12"
                    sm="6"
                    md="10"
                    lg="10"
                >
                  <v-text-field
                      outlined
                      label="Workload name"
                      v-model="workloadName"
                  ></v-text-field>

                  <v-textarea
                      outlined
                      label="Create Table Statement"
                      v-model="createTableDef"
                      v-on:blur="parseStatement()"
                  ></v-textarea>

                </v-col>

                <v-col cols="12">
                  <v-btn :title="save_title" v-if="parseSuccess" v-on:click="saveWorkloadToWorkspace()">{{ save_button }}</v-btn>
                  <v-btn :title="dl_title" v-if="parseSuccess" v-on:click="downloadWorkload()">{{ dl_button }}</v-btn>
                </v-col>
              </v-card>
            </v-flex>

          </v-layout>


        </v-container>

      </v-main>
    </v-layout>

    <v-footer app>
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>
<script>
import antlr4 from "antlr4";
import {saveAs} from "file-saver";
import yamlDumper from "js-yaml";
import CQL3Parser from '@/antlr/CQL3Parser.js';
import CQL3Lexer from '@/antlr/CQL3Lexer.js';
import defaultYaml from 'assets/default.yaml';
import basictypes from 'assets/basictypes.yaml';
import WorkspaceSelector from "@/components/WorkspaceSelector";
import AppSelector from "@/components/AppSelector";
import MainAppBar from "@/components/MainAppBar";

export default {
  components: {
    MainAppBar,
    AppSelector,
    WorkspaceSelector
  },
  data(context) {
    let data = {
      enabled: false,
      createTableDef: "",
      workloadName: "",
      parseSuccess: false,
      blob: null,
    };
    return data;
  },
  computed: {
    save_button: function () {
      return "Save to workspace '" + this.$store.getters["workspaces/getWorkspace"] + "'";
    },
    dl_button: function () {
      return "Download as " + this.filename;
    },
    dl_title: function () {
      return "Click to download the workload as '" + this.filename + "'";
    },
    filename: function () {
      return this.workloadName + ".yaml";
    },
    save_title: function () {
      return "Click to save this workload in the '" + this.workspace + "' workspace, or change the workspace in the app bar first.\n"
    },
    workspace: function () {
      return this.$store.getters["workspaces/getWorkspace"]
    },
  },
  methods: {
    async parseStatement() {
      console.log(this.$data.createTableDef);

      const input = this.$data.createTableDef;

      const chars = new antlr4.InputStream(input);
      const lexer = new CQL3Lexer.CQL3Lexer(chars);

      lexer.strictMode = false; // do not use js strictMode

      const tokens = new antlr4.CommonTokenStream(lexer);
      const parser = new CQL3Parser.CQL3Parser(tokens);

      const context = parser.create_table_stmt();

      try {
        const keyspaceName = context.table_name().keyspace_name().getChild(0).getText()
        const tableName = context.table_name().table_name_noks().getChild(0).getText()

        const columnDefinitions = context.column_definitions().column_definition();

        let columns = [];
        let partitionKeys = [];
        let clusteringKeys = [];
        columnDefinitions.forEach(columnDef => {
          if (columnDef.column_name() != null) {
            columns.push({
              "name": columnDef.column_name().getText(),
              "type": columnDef.column_type().getText()
            })
          } else {
            const primaryKeyContext = columnDef.primary_key()
            if (primaryKeyContext.partition_key() != null) {
              const partitionKeysContext = primaryKeyContext.partition_key().column_name();
              partitionKeysContext.map((partitionKey, i) => {
                const partitionKeyName = partitionKey.getText()
                const col = {
                  "name": partitionKeyName,
                  "type": columns.filter(x => x.name == partitionKeyName)[0].type
                }
                partitionKeys.push(col)
              })
            }
            if (primaryKeyContext.clustering_column().length != 0) {
              const clusteringKeysContext = primaryKeyContext.clustering_column();
              clusteringKeysContext.map((clusteringKey, i) => {
                const clusteringKeyName = clusteringKey.getText()
                const col = {
                  "name": clusteringKeyName,
                  "type": columns.filter(x => x.name == clusteringKeyName)[0].type
                }
                clusteringKeys.push(col)
              })
            }

          }
        })

        columns = columns.filter(col => {
          return partitionKeys.filter(pk => pk.name == col.name).length == 0 && clusteringKeys.filter(cc => cc.name == col.name).length == 0
        })

        const allColumns = [].concat(columns, partitionKeys, clusteringKeys)

        this.$data.tableName = tableName;
        this.$data.keyspaceName = keyspaceName;
        this.$data.columns = columns;
        this.$data.clusteringKeys = clusteringKeys;
        this.$data.partitionKeys = partitionKeys;
        this.$data.allColumns = allColumns;

        console.log(this.$data)

        console.log(defaultYaml)

        // schema and bindings
        let createTableStatement = "CREATE TABLE IF NOT EXISTS <<keyspace:" + keyspaceName + ">>." + tableName + " (\n";

        console.log(basictypes)
        defaultYaml.bindings = {}
        allColumns.forEach(column => {
          let recipe = basictypes.bindings[column.type + "val"];
          if (recipe == undefined) {
            const chars = new antlr4.InputStream(column.type);
            const lexer = new CQL3Lexer.CQL3Lexer(chars);
            lexer.strictMode = false; // do not use js strictMode
            const tokens = new antlr4.CommonTokenStream(lexer);
            const parser = new CQL3Parser.CQL3Parser(tokens);

            const typeContext = parser.column_type();
            const collectionTypeContext = typeContext.data_type().collection_type();
            const collectionType = collectionTypeContext.children[0].getText();
            if (collectionType.toLowerCase() == "set") {
              const type = collectionTypeContext.children[2].getText();
              recipe = "Set(HashRange(1,<<set-count-" + column.name + ":5>>)," + basictypes.bindings[type + "val"] + ") -> java.util.Set"
            } else if (collectionType.toLowerCase() == "list") {
              const type = collectionTypeContext.children[2].getText();
              recipe = "List(HashRange(1,<<list-count-" + column.name + ":5>>)," + basictypes.bindings[type + "val"] + ") -> java.util.List"

            } else if (collectionType.toLowerCase() == "map") {
              const type1 = collectionTypeContext.children[2].getText();
              const type2 = collectionTypeContext.children[4].getText();
              recipe = "Map(HashRange(1,<<map-count-" + column.name + ":5>>)," + basictypes.bindings[type1 + "val"] + "," + basictypes.bindings[type2 + "val"] + ") -> java.util.Map"
            } else {
              alert("Could not generate recipe for type: " + column.type + " for column: " + column.name)
            }
          }
          defaultYaml.bindings[column.name] = recipe
          createTableStatement = createTableStatement + column.name + " " + column.type + ",\n";
        })

        let pk = "PRIMARY KEY (("
        pk = pk + partitionKeys.map(x => x.name).reduce((x, acc) => acc = acc + "," + x)
        pk = pk + ")"
        if (clusteringKeys.length > 0) {
          pk = pk + "," + clusteringKeys.map(x => x.name).reduce((x, acc) => acc = acc + "," + x)
        }
        pk = pk + ")"
        createTableStatement = createTableStatement + pk + "\n);"
        defaultYaml.blocks[0].statements[0] = {"create-table": createTableStatement}

        //rampup
        let insertStatement = "INSERT INTO <<keyspace:" + keyspaceName + ">>." + tableName + " (\n";
        insertStatement = insertStatement + allColumns.map(x => x.name).reduce((x, acc) => acc = acc + ",\n" + x) + "\n) VALUES (\n";
        insertStatement = insertStatement + allColumns.map(x => "{" + x.name + "}").reduce((x, acc) => acc = acc + ",\n" + x) + "\n);"

        defaultYaml.blocks[1].statements[0] = {"insert-rampup": insertStatement}

        //main-write
        defaultYaml.blocks[2].statements[0] = {"insert-main": insertStatement}

        //main-read-partition
        let readPartitionStatement = "SELECT * from <<keyspace:" + keyspaceName + ">>." + tableName + " WHERE ";
        readPartitionStatement = readPartitionStatement + partitionKeys.map(x => x.name + "={" + x.name + "}").reduce((x, acc) => acc = acc + " AND " + x);
        let readRowStatement = readPartitionStatement + ";";
        if (clusteringKeys.length > 0) {
          readPartitionStatement = readPartitionStatement + " AND " + clusteringKeys.map(x => x.name + "={" + x.name + "}").reduce((x, acc) => acc = acc + " AND " + x);
        }
        readPartitionStatement = readPartitionStatement + ";";

        defaultYaml.blocks[3].statements[0] = {"read-partition": readPartitionStatement}

        //main-read-row
        defaultYaml.blocks[4].statements[0] = {"read-row": readRowStatement}

        defaultYaml.description = this.$data.workloadName

        const yamlOutputText = yamlDumper.dump(defaultYaml)
        this.blob = new Blob([yamlOutputText], {type: "text/plain;charset=utf-8"});
        this.parseSuccess = true;
      } catch (e) {
        console.log("blur, invalid create table def")
        console.log(e)
      }

    },
    downloadWorkload() {
      saveAs(this.blob, this.$data.filename);
    },
    saveWorkloadToWorkspace() {
      this.$store.dispatch("workspaces/putFile",{
        workspace: this.workspace,
        filename: this.filename,
        content: this.blob
      })
    }
  },
  async asyncData({$axios, store}) {
    let enabled = await $axios.$get("/status")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    return {
      enabled: enabled,
    }
  },
}
</script>
<style>
/*.container {*/
/*  margin: 0 auto;*/
/*  display: flex;*/
/*  justify-content: center;*/
/*  align-items: center;*/
/*  text-align: center;*/
/*}*/

/*.title {*/
/*  font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,*/
/*  'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;*/
/*  display: block;*/
/*  font-weight: 300;*/
/*  font-size: 100px;*/
/*  color: #35495e;*/
/*  letter-spacing: 1px;*/
/*}*/

/*.subtitle {*/
/*  font-weight: 300;*/
/*  font-size: 42px;*/
/*  color: #526488;*/
/*  word-spacing: 5px;*/
/*  padding-bottom: 15px;*/
/*}*/

/*.links {*/
/*  padding-top: 15px;*/
/*}*/
</style>
