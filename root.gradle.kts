plugins {
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.preprocessorRoot)
}

preprocess {
    val fabric12108 = createNode("1.21.8-fabric", 12108, "yarn")
    val fabric12110 = createNode("1.21.10-fabric", 12110, "yarn")
    fabric12108.link(fabric12110)
}