package com.example.employee;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    private static final List<Employee> employees = new ArrayList<>();
    private static int nextId = 1;

    public static void main(String[] args) throws Exception {
        employees.add(new Employee(nextId++, "Alice", "HR", 50000));
        employees.add(new Employee(nextId++, "Bob", "IT", 65000));

        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/", Main::home);
        server.createContext("/add", Main::add);
        server.createContext("/delete", Main::delete);
        server.createContext("/style.css", Main::css);
        server.start();

        System.out.println("Employee Management System started.");
        System.out.println("Open http://localhost:9090");
    }

    private static void home(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            send(ex, 405, "Method Not Allowed", "text/plain");
            return;
        }

        StringBuilder rows = new StringBuilder();
        for (Employee e : employees) {
            rows.append("<tr><td>").append(e.getId()).append("</td>")
                .append("<td>").append(esc(e.getName())).append("</td>")
                .append("<td>").append(esc(e.getDepartment())).append("</td>")
                .append("<td>₹").append(String.format("%.2f", e.getSalary())).append("</td>")
                .append("<td><a class='delete' href='/delete?id=").append(e.getId())
                .append("'>Delete</a></td></tr>");
        }

        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Employee Management System</title>
          <link rel="stylesheet" href="/style.css">
        </head>
        <body>
          <header>
            <h1>Employee Management System</h1>
            <p>Java Backend + HTML & CSS Frontend</p>
          </header>
          <main class="container">
            <section class="card">
              <h2>Add Employee</h2>
              <form action="/add" method="post">
                <div class="form-grid">
                  <div><label>Name</label><input name="name" required></div>
                  <div><label>Department</label><input name="department" required></div>
                  <div><label>Salary</label><input name="salary" type="number" min="0" step="0.01" required></div>
                </div>
                <button type="submit">Add Employee</button>
              </form>
            </section>
            <section class="card">
              <div class="title-row">
                <h2>Employee List</h2>
                <span class="count">Total: %d</span>
              </div>
              <table>
                <thead><tr><th>ID</th><th>Name</th><th>Department</th><th>Salary</th><th>Action</th></tr></thead>
                <tbody>%s</tbody>
              </table>
            </section>
          </main>
        </body>
        </html>
        """.formatted(employees.size(), rows);

        send(ex, 200, html, "text/html");
    }

    private static void add(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
            redirect(ex, "/");
            return;
        }
        Map<String,String> f = parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        try {
            String name = f.getOrDefault("name", "").trim();
            String dept = f.getOrDefault("department", "").trim();
            double salary = Double.parseDouble(f.getOrDefault("salary", "0"));
            if (!name.isEmpty() && !dept.isEmpty() && salary >= 0)
                employees.add(new Employee(nextId++, name, dept, salary));
        } catch (NumberFormatException ignored) {}
        redirect(ex, "/");
    }

    private static void delete(HttpExchange ex) throws IOException {
        Map<String,String> p = parse(Optional.ofNullable(ex.getRequestURI().getQuery()).orElse(""));
        try {
            int id = Integer.parseInt(p.getOrDefault("id", "-1"));
            employees.removeIf(e -> e.getId() == id);
        } catch (NumberFormatException ignored) {}
        redirect(ex, "/");
    }

    private static void css(HttpExchange ex) throws IOException {
        send(ex, 200, Files.readString(Path.of("frontend", "style.css")), "text/css");
    }

    private static Map<String,String> parse(String data) throws UnsupportedEncodingException {
        Map<String,String> map = new HashMap<>();
        if (data.isEmpty()) return map;
        for (String pair : data.split("&")) {
            String[] p = pair.split("=", 2);
            map.put(URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                    p.length > 1 ? URLDecoder.decode(p[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }

    private static String esc(String s) {
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
}

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    private static void send(HttpExchange ex, int status, String body, String type) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(data); }
    }
}
