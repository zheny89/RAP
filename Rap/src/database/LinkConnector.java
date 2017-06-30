package database;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import exception.EntryAlreadyExistsException;
import exception.EntryNotExistsException;

public class LinkConnector {

	private static final String PERSISTENCE_UNIT_NAME = "workers";
	private static EntityManagerFactory factory;
	public static EntityManager entityManager;
	
	public static Date currentDate() {
		return Date.valueOf(LocalDate.now());
	}
    
    public static void connect() {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		entityManager = factory.createEntityManager();
	}

	public static boolean isConnected() {
		return entityManager.isOpen();
	}

	public static void close() {
		entityManager.close();
	}

	/**
	 * �������� ��������� � ���� ������
	 * @param login ����� (��������� � ldap)
	 * @param name ���
	 * @param isAdmin ����� ��������������
	 * @throws EntryAlreadyExistsException ���� �������� � ����� ������� ��� ����������
	 */
	public static void addWorker(String login, String name, boolean isAdmin) throws EntryAlreadyExistsException {
		Worker worker = getWorker(login);
		if (worker != null) throw new EntryAlreadyExistsException("User with login=\'" + login + "\' already exists");
		entityManager.getTransaction().begin();
		worker = new Worker();
		worker.setLogin(login);
		worker.setAdmin(isAdmin);
		worker.setFlag(Worker.Flags.NONE);
		worker.setName(name);
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}

	/**
	 * @param id ��������� � ����
	 * @return ������ ���������, null ���� ��������� ��� � ����
	 */
	public static Worker getWorker(int id) {
		Worker worker = entityManager.find(Worker.class, id);
		return worker;
	}

	/**
	 * 
	 * @param login ��������� � ���� � ldap
	 * @return ������ ���������, null ���� ��������� ��� � ����
	 */
	public static Worker getWorker(String login) {
		Query q = entityManager.createQuery("SELECT w FROM Worker w WHERE w.login = \'" + login + "\'");
		try {
			return (Worker) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	private static void addWorktime(int workerId, LocalDate day, short hours, short flag) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		entityManager.getTransaction().begin();
		Worktime worktime = new Worktime();
		worktime.setDay(Date.valueOf(day));
		worktime.setFlag(flag);
		worktime.setHours(hours);
		worktime.setWorker(worker);
		entityManager.persist(worktime);
		entityManager.getTransaction().commit();
	}

	/**
	 * �������� ��������� � ���������� �� ������� ����
	 * @param id ���������
	 * @throws EntryNotExistsException ���� ��������� ��� � ����
	 */
	public static void logWorkerIn(int id) throws EntryNotExistsException {
		if (getWorktime(id, LocalDate.now()) == null) // ��� �� ��������� �������
			addWorktime(id, LocalDate.now(), (short) 8, Worker.Flags.NONE);
	}

	/**
	 * �������� ��������� ������ � ����
	 * @param senderId id �����������
	 * @param text ����� ���������
	 * @throws EntryNotExistsException ���� ����������� ��� � ����
	 */
	public static void addMessage(int senderId, String text) throws EntryNotExistsException {
		Worker sender = getWorker(senderId);
		if (sender == null) throw new EntryNotExistsException("No worker found with id = " + senderId);
		entityManager.getTransaction().begin();
		Message message = new Message();
		message.setDay(currentDate());
		message.setMessage(text);
		message.setSender(sender);
		message.setStatus(Message.Status.UNREAD);
		entityManager.persist(message);
		entityManager.getTransaction().commit();
	}
	
	/**
	 * 
	 * @param id ������ � ����������
	 * @return
	 */
	public static Worktime getWorktime(WorktimeKey id) {
		Worktime worktime = entityManager.find(Worktime.class, id);
		return worktime;
	}
	
	/**
	 * @param workerId id ���������
	 * @param day ����
	 * @return ������ � ������ ������������ � ��������� ����, null ���� �������� �� ���������
	 * @throws EntryNotExistsException ���� ��������� ��� � ����
	 */
	public static Worktime getWorktime(int workerId, LocalDate day) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		WorktimeKey worktimeKey = new WorktimeKey(workerId);
		worktimeKey.setDay(Date.valueOf(day));
		return getWorktime(worktimeKey);
	}

	/**
	 * �������� ���������� ����� ������ ��������� � ���������� ���� (���� ���� �� ���������)
	 * @param workerId id ���������
	 * @param day ����
	 * @param workHours ����� ����� ������
	 * @throws EntryNotExistsException 
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours) throws EntryNotExistsException {
		updateWorktimeHours(workerId, day, workHours, Worker.Flags.NONE);
	}
	
	/**
	 * �������� ���������� ����� ������ � ���� ��������� � ���������� ���� (���� ���� �� ���������)
	 * @param workerId id ���������
	 * @param day ����
	 * @param workHours ����� ����� ������
	 * @param flag ��������� �� ����
	 * @throws EntryNotExistsException 
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours, short flag) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		Worktime worktime = getWorktime(workerId, day);
		if (worktime == null) // �� ���������
			addWorktime(workerId, day, workHours, Worker.Flags.NONE);
		else { // ���������
			entityManager.getTransaction().begin();
			worktime.setHours(workHours);
			worktime.setFlag(flag);
			entityManager.persist(worktime);
			entityManager.getTransaction().commit();
		}
	}

	/**
	 * ��������� ��������� ��������� (����� ���������)
	 * @param id ���������
	 * @param flag ���������
	 * @param startDate ���� ������ �������� �����, null ���� � ������������ ���
	 */
	public static void updateWorkerFlag(int id, short flag, LocalDate startDate) {
		Worker worker = getWorker(id);
		entityManager.getTransaction().begin();
		worker.setFlag(flag);
		if (startDate != null)
			worker.setFlagStartDate(Date.valueOf(startDate));
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}

	/** 
	 * �������� ��������� ���������
	 * @param id ���������
	 */
	public static void clearWorkerFlag(int id) {
		updateWorkerFlag(id, Worker.Flags.NONE, null);
	}

	/**
	 * @return ������ ����������
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkers() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w");
		return q.getResultList();
	}
	
	/**
	 * @return ������ ����������
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkersSortedByName() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w ORDER BY w.name");
		return q.getResultList();
	}
	
	/**
	 * @return ������ ����������
	 */
	@SuppressWarnings("unchecked")
	public static List<Worktime> getWorktimes() {
		Query q = entityManager.createQuery("SELECT w FROM Worktime w");
		return q.getResultList();
	}
	
	/**
	 * @param messageStatus ������� ���������/�����������
	 * @return ������ ��������� �� ��������
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getMessages(short messageStatus) {
		Query q = entityManager.createQuery("SELECT m FROM Message m WHERE m.status = " + Short.toString(messageStatus));
		return q.getResultList();
	}

}
