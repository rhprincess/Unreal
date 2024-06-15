package io.twinkle.unreal

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
class MainEntry : IYukiHookXposedInit {
    override fun onInit() = YukiHookAPI.configs {
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = YukiHookAPI.encase {
        loadApp {
            if (packageName == "io.twinkle.unreal") {
                "io.twinkle.unreal.util.ModuleStatus".toClass().method {
                    name = "isActivated"
                    emptyParam()
                    returnType = BooleanType
                }.hook {
                    before {
                        this.resultTrue()
                    }
                }
                return@encase
            }
        }
    }

}
