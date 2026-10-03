package app.familygem.util

import app.familygem.Global
import org.folg.gedcom.model.Gedcom
import org.folg.gedcom.model.Person

/** The direct line of a person: all their ancestors and all their descendants, without siblings, uncles, cousins and partners. */
class Lineage(person: Person, gedcom: Gedcom = Global.gc) {

    /** IDs of the person, of their ancestors and of their descendants. */
    val persons: Set<String>

    /** IDs of the families that connect a parent to a child along the line. */
    val families: Set<String>

    init {
        val personIds = HashSet<String>()
        val familyIds = HashSet<String>()
        personIds.add(person.id)
        // Ascends through the parent families
        val stack = ArrayDeque<Person>()
        stack.add(person)
        while (stack.isNotEmpty()) {
            stack.removeLast().getParentFamilies(gedcom).forEach { family ->
                familyIds.add(family.id)
                family.getSpouses(gedcom).forEach { parent ->
                    if (personIds.add(parent.id)) stack.add(parent) // Already found persons are skipped, also to avoid loops
                }
            }
        }
        // Descends through the spouse families
        stack.add(person)
        while (stack.isNotEmpty()) {
            stack.removeLast().getSpouseFamilies(gedcom).forEach { family ->
                val children = family.getChildren(gedcom)
                if (children.isNotEmpty()) familyIds.add(family.id)
                children.forEach { child ->
                    if (personIds.add(child.id)) stack.add(child)
                }
            }
        }
        persons = personIds
        families = familyIds
    }
}
