package jsondoc

/**
 * Created by stevben on 28/12/13.
 */
class GroovyTimerTask extends TimerTask {
    Closure closure
    void run() {
        closure()
    }
}

class TimerMethods {
    static TimerTask runEvery(Timer timer, long delay, long period, Closure codeToRun) {
        TimerTask task = new GroovyTimerTask(closure: codeToRun)
        timer.schedule task, delay, period
        task
    }
}