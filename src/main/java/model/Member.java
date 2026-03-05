package model;

public class Member {
    private int memberId;
    private String firstName;
    private String lastName;
    private String phone;
    private String planName;
    private String trainerName;
    private boolean isCheckedIn;
    private String lastCheckIn;

    public Member(int memberId, String firstName, String lastName, String phone, String planName, String trainerName, boolean isCheckedIn, String lastCheckIn) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.planName = planName;
        this.trainerName = trainerName;
        this.isCheckedIn = isCheckedIn;
        this.lastCheckIn = lastCheckIn;
    }

    public int getMemberId() { return memberId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getPlanName() { return planName; }
    public String getTrainerName() { return trainerName; }
    public boolean isCheckedIn() { return isCheckedIn; }
    public String getLastCheckIn() { return lastCheckIn; }
}