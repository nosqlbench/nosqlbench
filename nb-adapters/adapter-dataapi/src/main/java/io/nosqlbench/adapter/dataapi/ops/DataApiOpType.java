package io.nosqlbench.adapter.dataapi.ops;

public enum DataApiOpType {
    create_collection,
    insert_many,
    insert_one,
    find,
    find_one,
    find_one_and_delete,
    find_one_and_update,
    update_one,
    update_many,
    delete_one,
    delete_many,
    delete_collection
}
