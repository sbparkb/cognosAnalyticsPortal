

import java.sql.Timestamp;

public class ReportInfo {

	private String menuDiv;
	private String reportId;
	private String upReportId;
	private String reportNm;
	private String activeYn;
	private String execUrl;
	private String packageNm;
	private String reportDiv;
	private String term;
	private String deptCd;
	private String reportOrg;
	private String reportContents;
	private String job;
	private Timestamp crtTime;
	private Timestamp discardTime;
	private String crtId;
	private String updId;
	private Timestamp updTime;
	private String deptNm;
	private String reportLevl;
	
	public String getMenuDiv() {
		return menuDiv;
	}
	public void setMenuDiv(String menuDiv) {
		this.menuDiv = menuDiv;
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getUpReportId() {
		return upReportId;
	}
	public void setUpReportId(String upReportId) {
		this.upReportId = upReportId;
	}
	public String getReportNm() {
		return reportNm;
	}
	public void setReportNm(String reportNm) {
		this.reportNm = reportNm;
	}
	public String getActiveYn() {
		return activeYn;
	}
	public void setActiveYn(String activeYn) {
		this.activeYn = activeYn;
	}
	public String getExecUrl() {
		return execUrl;
	}
	public void setExecUrl(String execUrl) {
		this.execUrl = execUrl;
	}
	public String getPackageNm() {
		return packageNm;
	}
	public void setPackageNm(String packageNm) {
		this.packageNm = packageNm;
	}
	public String getReportDiv() {
		return reportDiv;
	}
	public void setReportDiv(String reportDiv) {
		this.reportDiv = reportDiv;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getDeptCd() {
		return deptCd;
	}
	public void setDeptCd(String deptCd) {
		this.deptCd = deptCd;
	}
	public String getReportOrg() {
		return reportOrg;
	}
	public void setReportOrg(String reportOrg) {
		this.reportOrg = reportOrg;
	}
	public String getReportContents() {
		return reportContents;
	}
	public void setReportContents(String reportContents) {
		this.reportContents = reportContents;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public Timestamp getCrtTime() {
		return crtTime;
	}
	public void setCrtTime(Timestamp crtTime) {
		this.crtTime = crtTime;
	}
	public Timestamp getDiscardTime() {
		return discardTime;
	}
	public void setDiscardTime(Timestamp discardTime) {
		this.discardTime = discardTime;
	}
	public String getCrtId() {
		return crtId;
	}
	public void setCrtId(String crtId) {
		this.crtId = crtId;
	}
	public String getUpdId() {
		return updId;
	}
	public void setUpdId(String updId) {
		this.updId = updId;
	}
	public Timestamp getUpdTime() {
		return updTime;
	}
	public void setUpdTime(Timestamp updTime) {
		this.updTime = updTime;
	}
	public String getDeptNm() {
		return deptNm;
	}
	public void setDeptNm(String deptNm) {
		this.deptNm = deptNm;
	}
	public String getReportLevl() {
		return reportLevl;
	}
	public void setReportLevl(String reportLevl) {
		this.reportLevl = reportLevl;
	}
	@Override
	public String toString() {
		return "ReportInfo [menuDiv=" + menuDiv + ", reportId=" + reportId
				+ ", upReportId=" + upReportId + ", reportNm=" + reportNm
				+ ", activeYn=" + activeYn + ", execUrl=" + execUrl
				+ ", packageNm=" + packageNm + ", reportDiv=" + reportDiv
				+ ", term=" + term + ", deptCd=" + deptCd + ", reportOrg="
				+ reportOrg + ", reportContents=" + reportContents + ", job="
				+ job + ", crtTime=" + crtTime + ", discardTime=" + discardTime
				+ ", crtId=" + crtId + ", updId=" + updId + ", updTime="
				+ updTime + ", deptNm=" + deptNm + ", reportLevl=" + reportLevl
				+ "]";
	}	
		
}
