package be.cytomine.search.engine

/**
 * Created by lrollus on 7/23/14.
 */
class ResultSearch {
    Long id
    String className
    List<String> name
    Date date

    public ResultSearch(Long id,String className,List<String> name,Date date) {
        this.id = id
        this.className = className
        this.name = name
        this.date = date
    }

    public void addNewResult(List<String> name,Date date) {
        if(date && this.date && date.getTime()>this.date.getTime()) {
            this.date = date;
        }
        this.name.addAll(name)
    }

    public static void main(String[] args) {

        List<ResultSearch> results1 = [new ResultSearch(1,"dd",["ddd"],null),new ResultSearch(2,"dd",["ddd"],null),new ResultSearch(3,"dd",["ddd"],null)]
        List<ResultSearch> results2 = [new ResultSearch(1,"dd",["ddd"],null),new ResultSearch(2,"dd",["xxx"],null),new ResultSearch(4,"dd",["ddd"],null)]

        List<List<ResultSearch>> all = [results1,results2]

        List<ResultSearch> finalList = []

        all.first().each { result ->
            boolean presentInEach = true
            all.eachWithIndex { resultComp, index ->
                if(index!=0) {
                    boolean find = false
                    resultComp.each { res ->
                        println result.id

                        if(res.id == result.id) {
                            result.addNewResult(res.name,res.date)
                            find = true
                        }

                    }
                    if(!find) presentInEach = false
                }
            }
            if(presentInEach) {
                finalList << result
            }
        }
        println finalList
    }

    @Override
    public boolean equals(Object obj) {
        println "equals"

        if (obj instanceof ResultSearch) {
            ResultSearch other = (ResultSearch) obj;

            // Pour les attributs de type primitif
            // on compare directement les valeurs :
            if (this.id != other.id) {
                return false; // les attributs sont différents
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        println "hashCode"
        return id
    }

    public Map result() {
        return [id:id,name:name,className:className,date:date]
    }

    public String toString() { return this.id + " " + this.name }
}
