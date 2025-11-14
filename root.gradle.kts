plugins {
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.preprocessorRoot)
}

preprocess {
    val fabric12108 = createNode("1.21.8-fabric", 12108, "yarn")

    //fabric12106.link(fabric12105)
}