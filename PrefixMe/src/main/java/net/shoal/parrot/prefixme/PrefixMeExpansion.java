package net.shoal.parrot.prefixme;

import org.serverct.parrot.parrotx.hooks.BaseExpansion;

public class PrefixMeExpansion extends BaseExpansion {
    public PrefixMeExpansion() {
        super(PrefixMe.getInst());

        addParam(PlaceholderParam.builder()
                .name("prefix")
                .parse((user, args) -> PrefixManager.getInst().get(user.getUniqueId()))
                .build());
    }


}
