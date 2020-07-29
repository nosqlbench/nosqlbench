<template>
  <v-app>

    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench - Worlkoad Generator</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-layout
      justify-center
      align-center>

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
                            <v-textarea
                                    outlined
                                    label="Create Table Statement"
                                    v-model="createTableDef"
                                    v-on:blur="parseStatement()"
                            ></v-textarea>

                        </v-col>

                        <v-col cols="12">
                        </v-col>
                    </v-card>
                </v-flex>

            </v-layout>


        </v-container>

    </v-main>
    </v-layout>

      <v-footer app dark color="secondary">
          <span>&copy; 2020</span>
      </v-footer>

  </v-app>
</template>
<script>
    import get_data from '~/mixins/get_data.js';
    import antlr4 from "antlr4";
    import CQL3Parser from '~/antlr/CQL3Parser.js';
    import CQL3Lexer from '~/antlr/CQL3Lexer.js';

    export default {
        mixins: [get_data],
        components: {
        },
        computed: {
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

                const keyspaceName = context.table_name().keyspace_name().getChild(0).getText()
                const tableName = context.table_name().table_name_noks().getChild(0).getText()

                const columnDefinitions = context.column_definitions().column_definition();

                var columns = [];
                var partitionKeys = [];
                var clusteringKeys = [];
                columnDefinitions.forEach(columnDef => {
                    if (columnDef.column_name() != null) {
                        columns.push({"name": columnDef.column_name().getText(), "type": columnDef.column_type().getText()})
                    }else{
                        const primaryKeyContext = columnDef.primary_key()
                        if (primaryKeyContext.partition_key() != null){
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
                        if (primaryKeyContext.clustering_column().length != 0){
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

                this.$data.tableName = tableName;
                this.$data.keyspaceName = keyspaceName;
                this.$data.columns = columns;
                this.$data.clusteringKeys = clusteringKeys;
                this.$data.partitionKeys = partitionKeys;

            },
        },
        data(context) {
            let data = {
                enabled: false,
                createTableDef: "",
            };
            return data;
        },
        async asyncData({ $axios, store }) {
            let enabled = await $axios.$get("/services/nb/enabled")
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
    .container {
        margin: 0 auto;
        display: flex;
        justify-content: center;
        align-items: center;
        text-align: center;
    }

    .title {
        font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,
        'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        display: block;
        font-weight: 300;
        font-size: 100px;
        color: #35495e;
        letter-spacing: 1px;
    }

    .subtitle {
        font-weight: 300;
        font-size: 42px;
        color: #526488;
        word-spacing: 5px;
        padding-bottom: 15px;
    }

    .links {
        padding-top: 15px;
    }
</style>
