/*
 * Copyright (c) 2023-2025 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.nahkd123.stonks.utils.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.nahkd123.stonks.utils.OneOf;
import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec.ObjectField;
import io.github.nahkd123.stonks.utils.dynamic.json.JsonDynamicReader;
import io.github.nahkd123.stonks.utils.dynamic.json.JsonDynamicWriter;

class DynamicCodecTest {
	@Test
	void testObject() throws IOException {
		class Employee {
			private String name;
			private int age;
			private String role;

			public Employee(String name, int age, String role) {
				this.name = name;
				this.age = age;
				this.role = role;
			}

			public Employee() {}

			public String getName() { return name; }

			public void setName(String name) { this.name = name; }

			public int getAge() { return age; }

			public void setAge(int age) { this.age = age; }

			public String getRole() { return role; }

			public void setRole(String role) { this.role = role; }
		}

		DynamicCodec<Employee> codec = DynamicCodec.object(Employee::new, Map.of(
			"name", new ObjectField<>(DynamicCodec.STRING, Employee::getName, Employee::setName),
			"age", new ObjectField<>(DynamicCodec.INTEGER, Employee::getAge, Employee::setAge),
			"role", new ObjectField<>(DynamicCodec.STRING, Employee::getRole, Employee::setRole)));

		StringWriter writer = new StringWriter();
		codec.encodeTo(new JsonDynamicWriter(writer), new Employee("Alice", 24, "Designer"));
		Employee employee = codec.decodeFrom(new JsonDynamicReader(new StringReader(writer.toString())));
		assertEquals("Alice", employee.getName());
		assertEquals(24, employee.getAge());
		assertEquals("Designer", employee.getRole());

		DynamicCodec<OneOf<Employee, String>> or = codec.or(DynamicCodec.STRING);
		employee = ((OneOf.First<Employee, String>) or
			.decodeFrom(new JsonDynamicReader(new StringReader(writer.toString())))).a();

		writer = new StringWriter();
		or.encodeTo(new JsonDynamicWriter(writer), new OneOf.Second<>("Alice, 24, Designer"));
		assertEquals("\"Alice, 24, Designer\"", writer.toString());
	}
}
