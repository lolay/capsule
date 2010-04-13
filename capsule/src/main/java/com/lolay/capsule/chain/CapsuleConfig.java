package com.lolay.capsule.chain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.logging.LogHelper;

/**
 * A simple class to hold configuration data.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class CapsuleConfig {
	
	private final static Log setSizeLog = LogFactory.getLog(CapsuleConfig.class.getName() + ".setSize");
	private final static Log setMaxAgeLog = LogFactory.getLog(CapsuleConfig.class.getName() + ".setMaxAge");
	private final static Log setWarningThresholdLog = LogFactory.getLog(CapsuleConfig.class.getName() + ".setWarningThreshold");
	private final static Log setErrorThresholdLog = LogFactory.getLog(CapsuleConfig.class.getName() + ".setErrorThreshold");

	private String processingChain;
	private Integer size;
	private String domain;
	private String path;
	private Integer maxAge;
	private Integer warningThreshold;
	private Integer errorThreshold;
	private boolean filterEnabled = false;

	public String getProcessingChain() {
		return processingChain;
	}
	public void setProcessingChain(String processingChain) {
		if (processingChain != null) {
			this.processingChain = processingChain;
		}
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		if (size != null) {
			this.size = size;
		}
	}
	public void setSize(String size) {
		if (size != null) {
			try {
				this.size = Integer.valueOf(size);
			}
			catch (Exception e) {
				LogHelper.error(setSizeLog, "Exception in setSize [size={0}, exception={1}]", e, size);
			}
		}
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		if (domain != null) {
			this.domain = domain;
		}
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		if (path != null) {
			this.path = path;
		}
	}
	public Integer getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(Integer maxAge) {
		if (maxAge != null) {
			this.maxAge = maxAge;
			
		}
	}
	public void setMaxAge(String maxAge) {
		if (maxAge != null) {
			try {
				this.maxAge = Integer.valueOf(maxAge);
			}
			catch (Exception e) {
				LogHelper.error(setMaxAgeLog, "Exception setting maxAge [maxAge={0}, exception={1}]", e, maxAge);
			}
		}
	}
	
	public Integer getWarningThreshold() {
		return warningThreshold;
	}
	public void setWarningThreshold(Integer warningThreshold) {
		if (warningThreshold != null) {
			this.warningThreshold = warningThreshold;
		}
	}
	public void setWarningThreshold(String warningThreshold) {
		if (warningThreshold != null) {
			try {
				this.warningThreshold = Integer.valueOf(warningThreshold);
			}
			catch (Exception e) {
				LogHelper.error(setWarningThresholdLog, "Exception setting warningThreshold [warningThreshold={0}]", e, warningThreshold);
			}
		}
	}
	public Integer getErrorThreshold() {
		return errorThreshold;
	}
	public void setErrorThreshold(Integer errorThreshold) {
		if (errorThreshold != null) {
			this.errorThreshold = errorThreshold;
		}
	}
	public void setErrorThreshold(String errorThreshold) {
		if (errorThreshold != null) {
			try {
				this.errorThreshold = Integer.valueOf(errorThreshold);
			}
			catch (Exception e) {
				LogHelper.error(setErrorThresholdLog, "Exception setting errorThreshold [errorThreshold={0}]", e, errorThreshold);
			}
		}
	}
	
	public void setFilterEnabled(String filterEnabled) {
		if (filterEnabled != null) {
			this.filterEnabled = Boolean.valueOf(filterEnabled);
		}
	}
	public boolean isFilterEnabled() {
		return filterEnabled;
	}
	
	@Override
	public String toString() {
		return String.format("CapsuleConfig [processingChain=%1$s, size=%2$s, domain=%3$s, path=%4$s, maxAge=%5$s, filterEnabled=%6$s]", processingChain, size, domain, path, maxAge, filterEnabled);
	}

}
