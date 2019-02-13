

import java.util.ArrayList;

public class CognosObject {
	
	private String userName;
	private String searchPath;
	private String defaultName;
	private String reportId;
	private String fullDefaultName;
	private String shortDefaultName;
	private String encodeDefaultName;
	private String objectType;
	private String targetObjectType;
	private String targetSearchPath;
	private String description;
	private String storeId;
	private String executeUrl;
	private String defaultOutput;
	private String creationTime;
	private String parentStoreId;
	private String documentType;
	private String packageName;
	private boolean hasChildren;
	private int level;
	private String owner;
	private String modificationTime; 
	private ArrayList<CognosObject> subList; 
	private String linkUrl; 
	private boolean canExecute;
	private boolean isHidden;
	private String folderPath;
	private String folderDiv;	

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the searchPath
	 */
	public String getSearchPath() {
		return searchPath;
	}

	/**
	 * @param searchPath
	 *            the searchPath to set
	 */
	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}
	
	/**
	 * @return the defaultName
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * @param defaultName
	 *            the defaultName to set
	 */
	public void setDefaultName(String defaultName) {
		
		int start = defaultName.indexOf(".");
		int length = defaultName.length();
		if(start > 0){
			this.defaultName = defaultName.substring(start+1, length);
			this.reportId = defaultName.substring(0, start);
		}else{
			this.defaultName = defaultName;	
			this.reportId = defaultName;
		}
		setFullDefaultName(defaultName);
		setShortDefaultName(this.defaultName);
	}
	
	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getFullDefaultName() {
		return fullDefaultName;
	}

	public void setFullDefaultName(String  fullDefaultName) {
		this.fullDefaultName = fullDefaultName;
	}

	public String getShortDefaultName() {
		return shortDefaultName;
	}

	public void setShortDefaultName(String shortDefaultName) {
		
		int length = shortDefaultName.length();
		int cutLimit = 20;
		 		
		if(length <= cutLimit){
			this.shortDefaultName = shortDefaultName;
		}else{
			shortDefaultName = shortDefaultName.replaceAll(" ","  ");
			shortDefaultName = shortDefaultName.replaceAll("_","__");
			shortDefaultName = shortDefaultName.substring(0,cutLimit);
			shortDefaultName = shortDefaultName.replaceAll("  "," ");
			shortDefaultName = shortDefaultName.replaceAll("__","_");
			this.shortDefaultName = shortDefaultName.concat("...");
		}		
	}

	/**
	 * @return the encodeDefaultName
	 */
	public String getEncodeDefaultName() {
		return encodeDefaultName;
	}

	/**
	 * @param encodeDefaultName
	 *            the encodeDefaultName to set
	 */
	public void setEncodeDefaultName(String encodeDefaultName) {
		this.encodeDefaultName = encodeDefaultName;
	}

	/**
	 * @return the objectType
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getTargetObjectType() {
		return targetObjectType;
	}

	public void setTargetObjectType(String targetObjectType) {
		this.targetObjectType = targetObjectType;
	}

	public String getTargetSearchPath() {
		return targetSearchPath;
	}

	public void setTargetSearchPath(String targetSearchPath) {
		this.targetSearchPath = targetSearchPath;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the storeId
	 */
	public String getStoreId() {
		return storeId;
	}

	/**
	 * @param storeId
	 *            the storeId to set
	 */
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	/**
	 * @return the executeUrl
	 */
	public String getExecuteUrl() {
		return executeUrl;
	}

	/**
	 * @param executeUrl
	 *            the executeUrl to set
	 */
	public void setExecuteUrl(String executeUrl) {
		this.executeUrl = executeUrl;
	}

	/**
	 * @return the defaultOutput
	 */
	public String getDefaultOutput() {
		return defaultOutput;
	}

	/**
	 * @param defaultOutput
	 *            the defaultOutput to set
	 */
	public void setDefaultOutput(String defaultOutput) {
		this.defaultOutput = defaultOutput;
	}

	/**
	 * @return the creationTime
	 */
	public String getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 *            the creationTime to set
	 */
	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return the parentStoreId
	 */
	public String getParentStoreId() {
		return parentStoreId;
	}

	/**
	 * @param parentStoreId
	 *            the parentStoreId to set
	 */
	public void setParentStoreId(String parentStoreId) {
		this.parentStoreId = parentStoreId;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * @return the hasChildren
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}

	/**
	 * @param hasChildren
	 *            the hasChildren to set
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the modificationTime
	 */
	public String getModificationTime() {
		return modificationTime;
	}

	/**
	 * @param modificationTime
	 *            the modificationTime to set
	 */
	public void setModificationTime(String modificationTime) {
		this.modificationTime = modificationTime;
	}

	public ArrayList<CognosObject> getSubList() {
		return subList;
	}

	public void setSubList(ArrayList<CognosObject> subList) {
		this.subList = subList;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	public boolean isCanExecute() {
		return canExecute;
	}

	public void setCanExecute(boolean canExecute) {
		this.canExecute = canExecute;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getFolderDiv() {
		return folderDiv;
	}

	public void setFolderDiv(String folderDiv) {
		this.folderDiv = folderDiv;
	}
}