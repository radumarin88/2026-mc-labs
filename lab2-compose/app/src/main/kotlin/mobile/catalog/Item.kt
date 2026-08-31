package mobile.catalog

/** A catalog entry. In a real app this comes from a database or the network (Labs 5 and 6). */
data class Item(
    val id: String,
    val name: String,
    val category: String,
    val tagline: String,
    val description: String,
)

/** A fixed in-memory data source, standing in for the repository you build later. */
object SampleItems {
    val all: List<Item> = listOf(
        Item("edge", "Edge Node", "Compute", "Compute close to the user",
            "A small server near the network edge that runs work the phone cannot, " +
                "with far lower latency than a distant cloud region."),
        Item("cloudlet", "Cloudlet", "Compute", "A data center in a box",
            "A trusted, resource-rich computer one wireless hop away, used to offload " +
                "heavy computation from mobile devices."),
        Item("relay", "Mesh Relay", "Network", "Store, carry, forward",
            "A node that holds a message and physically carries it until it meets the " +
                "next hop, delivering data even without an end-to-end path."),
        Item("gateway", "IoT Gateway", "Network", "Bridge to the sensors",
            "A device that connects low-power sensors to the wider network, translating " +
                "between short-range radios and IP."),
        Item("cache", "Local Cache", "Storage", "On-device source of truth",
            "A local store the app reads first, so it keeps working offline and only syncs " +
                "when the network allows."),
        Item("vault", "Secure Vault", "Storage", "Keys never leave the device",
            "Hardware-backed storage for secrets, so sensitive material stays on the device " +
                "even if the app is compromised."),
    )

    /** The distinct categories present, in first-seen order. */
    val categories: List<String> = all.map { it.category }.distinct()

    fun byId(id: String): Item = all.first { it.id == id }
}
