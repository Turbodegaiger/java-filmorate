package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTests {
	private final UserDbStorage userStorage;
	private List<User> testUsers;
	private List<User> testUpdateUsers;

	@BeforeEach
	public void loadUsers() {
		testUsers = new ArrayList<>();
		testUpdateUsers = new ArrayList<>();
		User user1 = new User();
		user1.setEmail("aaaa@ya.ru");
		user1.setBirthday(DateUtility.formatToDate("2000-11-11"));
		user1.setLogin("aaaa");
		User user2 = new User();
		user2.setEmail("bbbb@ya.ru");
		user2.setLogin("bbbb");
		user2.setBirthday(DateUtility.formatToDate("2000-11-11"));
		testUsers.add(user1);
		testUsers.add(user2);
		User user3 = new User();
		user3.setId(1);
		user3.setEmail("aaaaweeq@ya.ru");
		user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
		user3.setLogin("aaaa");
		User user4 = new User();
		user4.setId(5);
		user4.setEmail("bbbbeqe@ya.ru");
		user4.setLogin("bbbba");
		user4.setBirthday(DateUtility.formatToDate("2000-11-12"));
		User user5 = new User();
		user5.setId(3);
		user5.setEmail("eeeee@ya.ru");
		user5.setLogin("eeeeaq");
		user5.setBirthday(DateUtility.formatToDate("2000-11-12"));
		testUpdateUsers.add(user3);
		testUpdateUsers.add(user4);
		testUpdateUsers.add(user5);
	}

	@Test
	public void testAddUserAndGetUser() {
		User newUser = new User(
				4, "adada", "addada@ya.ru", "woooow", DateUtility.formatToDate("1995-05-05"));
		User addedUser = userStorage.addUser(newUser);
		assertThat(userStorage.getUser(addedUser.getId()).get()).isEqualTo(newUser);
	}

	@Test
	public void testAddAlreadyExistUser() {
		assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(() -> userStorage.addUser(testUsers.get(0)));
	}

	@Test
	public void testWrongGetUser() {
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.getUser(0));
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.getUser(5));
	}

	@Test
	public void testGetUsers() {
		List<User> userList = FilmDbStorageTests.createUsers();
		assertThat(userStorage.getUsers()).isEqualTo(userList);
	}

	@Test
	public void testUpdateUser() {
		assertThat(userStorage.updateUser(testUpdateUsers.get(0))).isEqualTo(testUpdateUsers.get(0));
		assertThat(userStorage.getUser(1).get()).isEqualTo(testUpdateUsers.get(0));
	}

	@Test
	public void testWrongUpdateUser() {
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.updateUser(testUpdateUsers.get(1)));
	}

	@Test
	public void testRemoveUser() {
		User newUser = userStorage.addUser(testUpdateUsers.get(2));
		assertThat(userStorage.getUser(newUser.getId()).get()).isEqualTo(testUpdateUsers.get(2));
		userStorage.removeUser(3);
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.getUser(3));
	}

	@Test
	public void testWrongRemoveUser() {
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.removeUser(3));
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.removeUser(0));
	}

	@Test
	public void testAddFriendAndGetUserFriends() {
		userStorage.addFriend(1,2);
		assertThat(userStorage.getUserFriends(1)).isEqualTo(Set.of(2));
	}

	@Test
	public void testWrongAddFriend() {
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.addFriend(1,3));
		assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(() -> userStorage.addFriend(1,1));
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.addFriend(1,0));
	}

	@Test
	public void testRemoveFriend() {
		userStorage.addUser(new User(3, "qeq", "qqq@ya.ru", "login", DateUtility.formatToDate("2001-01-01")));
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of());
		userStorage.addFriend(2,1);
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of(1));
		userStorage.addFriend(2,3);
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of(1,3));
		userStorage.removeFriend(2,1);
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of(3));
		userStorage.removeFriend(2,3);
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of());
	}

	@Test
	public void testWrongRemoveFriend() {
		userStorage.addFriend(2,1);
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.removeFriend(2,0));
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> userStorage.removeFriend(2,3));
		assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(() -> userStorage.removeFriend(2,2));
		userStorage.removeFriend(2,1);
		assertThat(userStorage.getUserFriends(2)).isEqualTo(Set.of());
	}

	@Test
	public void testFindUserById() {
		Optional<User> userOptional = userStorage.getUser(1);
		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", 1)
				);
	}

}