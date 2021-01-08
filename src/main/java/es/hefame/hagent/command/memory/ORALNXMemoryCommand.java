
package es.hefame.hagent.command.memory;

public class ORALNXMemoryCommand extends LNXMemoryCommand
{
	/*
	 * @Override
	 * public List<PrtgResult> get_prtg_results() throws AgentHttpException
	 * {
	 * List<PrtgResult> memory_results = super.get_prtg_results();
	 * 
	 * // Canal de memoria compartida /dev/shm
	 * PrtgFilesystemsObserver observer = ObserverFactory.get_observer(PrtgFilesystemsObserver.class);
	 * List<PrtgResult> result = observer.get_last_result();
	 * 
	 * if (result != null)
	 * {
	 * Iterator<PrtgResult> it = result.iterator();
	 * while (it.hasNext())
	 * {
	 * PrtgResult tmp = it.next();
	 * if (tmp instanceof PrtgFilesystemResult)
	 * {
	 * PrtgFilesystemResult fs_result = (PrtgFilesystemResult) tmp;
	 * if (fs_result.get_mount_point().equals("/dev/shm"))
	 * {
	 * L.vrb(fs_result.toString());
	 * memory_results.add(fs_result);
	 * break;
	 * }
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * return memory_results;
	 * }
	 */
}
