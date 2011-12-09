package be.cytomine.test.http

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * 
 */
class DomainAPI {

   static boolean containsInJSONList(Long id, def list) {
       println "id="+id + " list="+list
       if(list==[]) return false
       boolean find = false
      list.each { item ->
          Long idItem=item.id
         if(idItem==id) {find=true}

      }
       return find
   }

}
