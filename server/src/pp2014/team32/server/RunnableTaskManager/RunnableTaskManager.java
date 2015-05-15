package pp2014.team32.server.RunnableTaskManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese statische Klasse verwaltet einen Thread Pool, dem ausfuehrbare
 * Runnables uebergeben werden koennen.
 * 
 * @author Peter Kings
 */
public class RunnableTaskManager {
	// Anzahl der Threads in dem Threadpool
	private final static int			threadAmount					= Integer.parseInt(PropertyManager.getProperty("server.RunnableTaskManagerThreadAmount"));
	// Groesse der Queue in der die Runnables abgelegt werden
	private final static int			threadQueueSize					= Integer.parseInt(PropertyManager.getProperty("server.RunnableTaskManagerThreadQueueSize"));
	// Threadpool
	private static ThreadPoolExecutor	RunnableTaskManagerThreadPool	= new ThreadPoolExecutor(threadAmount, threadAmount, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(threadQueueSize));

	/**
	 * Dieser Methode kann ein ausfuehrbarer Runnable uebergeben werden. Dieses
	 * wird dann einer Queue hinzugefuegt, von einer Anzahl an
	 * Threads abgearbeitet wird. (Wie viele Threads erstellt werden, wird in der
	 * Property Datei festgesetzt).
	 * 
	 * @author Peter Kings
	 * @param r das auszufuehrende Runnable
	 */
	public static void addRunnableTask(Runnable r) {
		RunnableTaskManagerThreadPool.submit(r);
	}
}
