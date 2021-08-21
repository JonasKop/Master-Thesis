import com.fkorotkov.kubernetes.*
import com.jonassjodin.cbtt.config.BuildTool
import com.jonassjodin.cbtt.lib.hash
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim
import io.fabric8.kubernetes.api.model.Quantity

fun checksum(buildTool: BuildTool) =
    hash(buildTool.localCacheDir!!)

class LocalCache(buildTool: BuildTool) : PersistentVolumeClaim() {
    init {
        metadata {
            name = buildTool.name
            annotations = mapOf(Pair("checksum", checksum(buildTool)))
        }
        spec {
            accessModes = listOf("ReadWriteOnce")
            resources {
                requests = mapOf(Pair("storage", Quantity("20Gi")))
            }
        }
    }
}