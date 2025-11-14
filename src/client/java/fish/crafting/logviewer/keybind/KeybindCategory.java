package fish.crafting.logviewer.keybind;

public enum KeybindCategory {

    SLV("slv"),
    ;

    private final String subid;

    KeybindCategory(String subid){
        this.subid = subid;
    }

    public String translation(){
        return "slv.keybind.category." + subid;
    }

}
