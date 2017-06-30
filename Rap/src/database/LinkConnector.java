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
	 * Добавить работника в базу данных
	 * @param login логин (совпадает с ldap)
	 * @param name имя
	 * @param isAdmin права администратора
	 * @throws EntryAlreadyExistsException если работник с таким логином уже существует
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
	 * @param id работника в базе
	 * @return данные работника, null если работника нет в базе
	 */
	public static Worker getWorker(int id) {
		Worker worker = entityManager.find(Worker.class, id);
		return worker;
	}

	/**
	 * 
	 * @param login работника в базе и ldap
	 * @return данные работника, null если работника нет в базе
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
	 * Отметить работника в расписании на текущий день
	 * @param id работника
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static void logWorkerIn(int id) throws EntryNotExistsException {
		if (getWorktime(id, LocalDate.now()) == null) // ещё не отмечался сегодня
			addWorktime(id, LocalDate.now(), (short) 8, Worker.Flags.NONE);
	}

	/**
	 * Добавить сообщение админу в базу
	 * @param senderId id отправителя
	 * @param text текст сообщения
	 * @throws EntryNotExistsException если отправителя нет в базе
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
	 * @param id записи в расписании
	 * @return
	 */
	public static Worktime getWorktime(WorktimeKey id) {
		Worktime worktime = entityManager.find(Worktime.class, id);
		return worktime;
	}
	
	/**
	 * @param workerId id работника
	 * @param day дата
	 * @return данные о работе пользователя в указанный день, null если работник не отмечался
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static Worktime getWorktime(int workerId, LocalDate day) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		WorktimeKey worktimeKey = new WorktimeKey(workerId);
		worktimeKey.setDay(Date.valueOf(day));
		return getWorktime(worktimeKey);
	}

	/**
	 * Изменить количество часов работы работника в конкретный день (даже если не отмечался)
	 * @param workerId id работника
	 * @param day дата
	 * @param workHours новое время работы
	 * @throws EntryNotExistsException 
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours) throws EntryNotExistsException {
		updateWorktimeHours(workerId, day, workHours, Worker.Flags.NONE);
	}
	
	/**
	 * Изменить количество часов работы и флаг работника в конкретный день (даже если не отмечался)
	 * @param workerId id работника
	 * @param day дата
	 * @param workHours новое время работы
	 * @param flag состояние на дату
	 * @throws EntryNotExistsException 
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours, short flag) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		Worktime worktime = getWorktime(workerId, day);
		if (worktime == null) // не отмечался
			addWorktime(workerId, day, workHours, Worker.Flags.NONE);
		else { // отмечался
			entityManager.getTransaction().begin();
			worktime.setHours(workHours);
			worktime.setFlag(flag);
			entityManager.persist(worktime);
			entityManager.getTransaction().commit();
		}
	}

	/**
	 * Выставить состояние работнику (можно отложенно)
	 * @param id работника
	 * @param flag состояние
	 * @param startDate дата начала действия флага, null если с сегодняшнего дня
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
	 * Сбросить состояние работника
	 * @param id работника
	 */
	public static void clearWorkerFlag(int id) {
		updateWorkerFlag(id, Worker.Flags.NONE, null);
	}

	/**
	 * @return список работников
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkers() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w");
		return q.getResultList();
	}
	
	/**
	 * @return список работников
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkersSortedByName() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w ORDER BY w.name");
		return q.getResultList();
	}
	
	/**
	 * @return список работников
	 */
	@SuppressWarnings("unchecked")
	public static List<Worktime> getWorktimes() {
		Query q = entityManager.createQuery("SELECT w FROM Worktime w");
		return q.getResultList();
	}
	
	/**
	 * @param messageStatus признак прочитано/непрочитано
	 * @return список сообщений по признаку
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getMessages(short messageStatus) {
		Query q = entityManager.createQuery("SELECT m FROM Message m WHERE m.status = " + Short.toString(messageStatus));
		return q.getResultList();
	}

}
