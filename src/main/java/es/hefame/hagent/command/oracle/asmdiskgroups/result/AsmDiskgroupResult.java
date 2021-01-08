package es.hefame.hagent.command.oracle.asmdiskgroups.result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hagent.command.filesystems.result.FilesystemResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.prtg.PrtgFilesystemsConfiguration.FilesystemConfigData;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgThresshold;

public class AsmDiskgroupResult extends FilesystemResult
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	OGRID_ASMDG_CMD_MARKER	= MarkerManager.getMarker("OGRID_ASMDG_CMD");

	private String				mount_point;
	private String				state;
	private int					max_mb;
	private int					used_mb;
	private int					free_mb;
	private float				percentage_used;
	private int					offline_disks;

	public AsmDiskgroupResult(String name, String state, int max_mb, int used_mb, int free_mb, float percentage_used, int offline_disks)
	{
		if (name.charAt(0) == '+') this.mount_point = name;
		else this.mount_point = '+' + name;
		this.state = state;
		this.max_mb = max_mb;
		this.used_mb = used_mb;
		this.free_mb = free_mb;
		this.percentage_used = percentage_used;
		this.offline_disks = offline_disks;

		this.channelize();
	}

	public AsmDiskgroupResult(String name, String state, String redundancy, int total_mb, int required_mirror_free_mb, int usable_file_mb, int offline_disks)
	{
		L.debug(OGRID_ASMDG_CMD_MARKER, "Analizando datos del diskgroup [{}]: state [{}], redund [{}], totalMB [{}], reqMirrFreeMb [{}], UsableMB [{}], OfflineDisks [{}]", name, state, redundancy, total_mb, required_mirror_free_mb, usable_file_mb, offline_disks);
		if (name.charAt(0) == '+') this.mount_point = name;
		this.mount_point = '+' + name;
		this.state = state;

		switch (redundancy)
		{
			case "EXTERN":
				this.max_mb = total_mb;
				break;
			case "NORMAL":
				this.max_mb = ((total_mb - required_mirror_free_mb) / 2);
				break;
			case "HIGH":
				this.max_mb = ((total_mb - required_mirror_free_mb) / 3);
				break;
		}

		this.free_mb = usable_file_mb;
		this.used_mb = this.max_mb - this.free_mb;
		this.percentage_used = (this.used_mb * 100) / this.max_mb;
		this.offline_disks = offline_disks;

		this.channelize();

		L.debug(OGRID_ASMDG_CMD_MARKER, "El resultado analizado es [{}]", this.jsonEncode());
	}

	public String get_mount_point()
	{
		return mount_point;
	}

	public int is_mounted()
	{
		if (this.state.equalsIgnoreCase("mounted")) return 1;
		return 0;
	}

	public long get_total_bytes()
	{
		return max_mb * DiskSizeConverter.SizeFactor.MEGA.factor;
	}

	public long get_used_bytes()
	{
		return used_mb * DiskSizeConverter.SizeFactor.MEGA.factor;
	}

	public long get_free_bytes()
	{
		return free_mb * DiskSizeConverter.SizeFactor.MEGA.factor;
	}

	public double get_used_bytes_percentage()
	{
		return percentage_used;
	}

	public int get_offline_disks()
	{
		return offline_disks;
	}

	protected void channelize()
	{
		FilesystemConfigData config_data = CONF.prtg.filesystems.get(this.get_mount_point());

		PrtgThresshold used_bytes_percent_thresshold = null;
		PrtgThresshold free_bytes_thresshold = null;

		if (config_data != null)
		{
			if (this.is_mounted() == 0)
			{
				if (config_data.check_mounted())
				{
					this.addChannel(new PrtgErrorResult("El diskgroup " + this.get_mount_point() + " no esta montado."));
				}
				return;
			}

			used_bytes_percent_thresshold = new PrtgThresshold(null, null, config_data.get_warn_percent(), config_data.get_error_percent());
			free_bytes_thresshold = new PrtgThresshold(config_data.get_error_free(), config_data.get_warn_free(), null, null);
		}

		this.addChannel(new PrtgChannelResult("Uso de " + this.get_mount_point(), this.get_used_bytes_percentage(), "Percent", used_bytes_percent_thresshold));
		// this.addChannel(new PrtgChannelResult("Bytes usados en " + this.get_name(), this.get_used_bytes(), "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Bytes libres en " + this.get_mount_point(), this.get_free_bytes(), "BytesDisk", free_bytes_thresshold));
		this.addChannel(new PrtgChannelResult("Bytes total en " + this.get_mount_point(), this.get_total_bytes(), "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Discos offline en " + this.get_mount_point(), this.get_offline_disks(), "Count", new PrtgThresshold(null, null, null, 0.5)));
	}

}
