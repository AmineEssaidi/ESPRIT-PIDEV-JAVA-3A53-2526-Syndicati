package com.syndicati.views.backend.dashboard;

import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.forum.Reaction;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

@SuppressWarnings("SpellCheckingInspection")
final class DashboardForumSection {

    private DashboardForumSection() {
    }

    static VBox build(DashboardView view) {
        return view.moduleModeView(
            "Forum",
            "\uD83D\uDCAC",
            new String[]{"Publications", "Commentaires", "Reactions"},
            key -> "Commentaires".equals(key) ? commentsPane(view) :
                   "Reactions".equals(key) ? reactionsPane(view) :
                   publicationsPane(view)
        );
    }

    private static VBox publicationsPane(DashboardView view) {
        VBox wrap = new VBox(16);
        List<Publication> publications = view.dashboardAdminService().publications();

        int total = publications.size();
        int withImages = 0;
        int recent = 0;
        int categories = 0;
        java.util.Set<String> uniqueCategories = new java.util.HashSet<>();
        java.time.LocalDate sevenDaysAgo = java.time.LocalDate.now().minusDays(7);

        for (Publication pub : publications) {
            if (pub.getImagePub() != null && !pub.getImagePub().isBlank()) {
                withImages++;
            }
            if (pub.getCategoriePub() != null) {
                uniqueCategories.add(pub.getCategoriePub());
            }
            if (pub.getDateCreationPub() != null && pub.getDateCreationPub().toLocalDate().isAfter(sevenDaysAgo)) {
                recent++;
            }
        }
        categories = uniqueCategories.size();

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        view.addStatCards(stats,
            new String[]{"TOT", "IMG", "NEW", "CAT"},
            new String[]{"Total", "With Images", "7-Day New", "Categories"},
            new String[]{String.valueOf(total), String.valueOf(withImages), String.valueOf(recent), String.valueOf(categories)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        List<String[]> baseRows = new ArrayList<>();
        for (Publication pub : publications) {
            String title = view.safe(pub.getTitrePub());
            String category = view.safe(pub.getCategoriePub());
            String description = view.safe(pub.getDescriptionPub());
            String image = view.safe(pub.getImagePub());
            String date = pub.getDateCreationPub() != null ? pub.getDateCreationPub().toLocalDate().toString() : "";

            baseRows.add(new String[]{
                title,
                category,
                description,
                image,
                date
            });
        }

        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);
        TextField searchField = dashboardSearchField(view, "Search publications by title, category, description or date...");
        Button sortPill = view.pillAction("Order: A-Z", false);
        HBox filterRow = new HBox(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox headerControls = dashboardHeaderControls(searchField, sortPill, filterRow);
        VBox tableHost = new VBox();

        String[][] filters = new String[][]{
            {"title", "Title"},
            {"category", "Category"},
            {"date", "Date"}
        };

        for (String[] filter : filters) {
            String key = filter[0];
            Button button = view.pillAction(filter[1], false);
            button.setOnAction(e -> {
                queryState.filterKey = key.equals(queryState.filterKey) ? "" : key;
                queryState.page = 1;
                renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Publications", "Publication", new String[]{"Title", "Category", "Description", "Image", "Date"}, "No publications found");
            });
            filterRow.getChildren().add(button);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Publications", "Publication", new String[]{"Title", "Category", "Description", "Image", "Date"}, "No publications found");
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Publications", "Publication", new String[]{"Title", "Category", "Description", "Image", "Date"}, "No publications found");
        });

        filterRow.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                styleQueryPill(view, button, false);
            }
        });

        renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Publications", "Publication", new String[]{"Title", "Category", "Description", "Image", "Date"}, "No publications found");

        wrap.getChildren().addAll(stats, tableHost);
        return wrap;
    }

    private static VBox commentsPane(DashboardView view) {
        VBox wrap = new VBox(16);
        List<Commentaire> commentaires = view.dashboardAdminService().commentaires();

        int total = commentaires.size();
        int withImages = 0;
        int recent = 0;
        int topAuthors = 0;
        java.util.Set<String> uniqueAuthors = new java.util.HashSet<>();
        java.time.LocalDate sevenDaysAgo = java.time.LocalDate.now().minusDays(7);

        for (Commentaire comment : commentaires) {
            if (comment.getImageCommentaire() != null && !comment.getImageCommentaire().isBlank()) {
                withImages++;
            }
            if (comment.getUser() != null) {
                uniqueAuthors.add(comment.getUser().getIdUser() != null ? comment.getUser().getIdUser().toString() : "unknown");
            }
            if (comment.getCreatedAt() != null && comment.getCreatedAt().toLocalDate().isAfter(sevenDaysAgo)) {
                recent++;
            }
        }
        topAuthors = uniqueAuthors.size();

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        view.addStatCards(stats,
            new String[]{"TOT", "IMG", "NEW", "AUT"},
            new String[]{"Total", "With Images", "7-Day New", "Authors"},
            new String[]{String.valueOf(total), String.valueOf(withImages), String.valueOf(recent), String.valueOf(topAuthors)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        List<String[]> baseRows = new ArrayList<>();
        for (Commentaire comment : commentaires) {
            String publication = comment.getPublication() != null ? view.safe(comment.getPublication().getTitrePub()) : "Unknown";
            String author = comment.getUser() != null ? view.safe(comment.getUser().getFirstName()) + " " + view.safe(comment.getUser().getLastName()) : "Anonymous";
            String description = view.safe(comment.getDescriptionCommentaire());
            String image = view.safe(comment.getImageCommentaire());
            String date = comment.getCreatedAt() != null ? comment.getCreatedAt().toLocalDate().toString() : "";

            baseRows.add(new String[]{
                publication,
                author.trim(),
                description,
                image,
                date
            });
        }

        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);
        TextField searchField = dashboardSearchField(view, "Search comments by publication, author, description or date...");
        Button sortPill = view.pillAction("Order: A-Z", false);
        HBox filterRow = new HBox(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox headerControls = dashboardHeaderControls(searchField, sortPill, filterRow);
        VBox tableHost = new VBox();

        String[][] filters = new String[][]{
            {"publication", "Publication"},
            {"author", "Author"},
            {"date", "Date"}
        };

        for (String[] filter : filters) {
            String key = filter[0];
            Button button = view.pillAction(filter[1], false);
            button.setOnAction(e -> {
                queryState.filterKey = key.equals(queryState.filterKey) ? "" : key;
                queryState.page = 1;
                renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Comments", "Comment", new String[]{"Publication", "Author", "Description", "Image", "Date"}, "No comments found");
            });
            filterRow.getChildren().add(button);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Comments", "Comment", new String[]{"Publication", "Author", "Description", "Image", "Date"}, "No comments found");
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Comments", "Comment", new String[]{"Publication", "Author", "Description", "Image", "Date"}, "No comments found");
        });

        filterRow.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                styleQueryPill(view, button, false);
            }
        });

        renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Comments", "Comment", new String[]{"Publication", "Author", "Description", "Image", "Date"}, "No comments found");

        wrap.getChildren().addAll(stats, tableHost);
        return wrap;
    }

    private static VBox reactionsPane(DashboardView view) {
        VBox wrap = new VBox(16);
        List<Reaction> reactions = view.dashboardAdminService().reactions();

        int total = reactions.size();
        int onPosts = 0;
        int onComments = 0;
        int topUsers = 0;
        java.util.Set<String> uniqueUsers = new java.util.HashSet<>();

        for (Reaction reaction : reactions) {
            if (reaction.getPublication() != null) {
                onPosts++;
            } else if (reaction.getCommentaire() != null) {
                onComments++;
            }
            if (reaction.getUser() != null) {
                uniqueUsers.add(reaction.getUser().getIdUser() != null ? reaction.getUser().getIdUser().toString() : "unknown");
            }
        }
        topUsers = uniqueUsers.size();

        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        view.addStatCards(stats,
            new String[]{"TOT", "PUB", "CMT", "USR"},
            new String[]{"Total", "On Posts", "On Comments", "Users"},
            new String[]{String.valueOf(total), String.valueOf(onPosts), String.valueOf(onComments), String.valueOf(topUsers)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        List<String[]> baseRows = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd");

        for (Reaction reaction : reactions) {
            String user = reaction.getUser() != null ? view.safe(reaction.getUser().getFirstName()) + " " + view.safe(reaction.getUser().getLastName()) : "Anonymous";
            user = user.trim();
            
            String target = "";
            if (reaction.getPublication() != null) {
                target = "Pub: " + view.safe(reaction.getPublication().getTitrePub());
            } else if (reaction.getCommentaire() != null) {
                target = "Comment";
            } else {
                target = "Unknown";
            }
            
            String kind = view.safe(reaction.getKind());
            String date = reaction.getUpdatedAt() != null ? reaction.getUpdatedAt().format(dateFormat) : "N/A";

            baseRows.add(new String[]{
                user,
                target,
                kind,
                "1",
                date
            });
        }

        DashboardTableQueryEngine.QueryState queryState = new DashboardTableQueryEngine.QueryState(1_000_000);
        TextField searchField = dashboardSearchField(view, "Search reactions by user, target, type or date...");
        Button sortPill = view.pillAction("Order: A-Z", false);
        HBox filterRow = new HBox(6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox headerControls = dashboardHeaderControls(searchField, sortPill, filterRow);
        VBox tableHost = new VBox();

        String[][] filters = new String[][]{
            {"user", "User"},
            {"target", "Target"},
            {"type", "Type"},
            {"date", "Date"}
        };

        for (String[] filter : filters) {
            String key = filter[0];
            Button button = view.pillAction(filter[1], false);
            button.setOnAction(e -> {
                queryState.filterKey = key.equals(queryState.filterKey) ? "" : key;
                queryState.page = 1;
                renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Reactions", "Reaction", new String[]{"User", "Target", "Type", "Count", "Updated"}, "No reactions found");
            });
            filterRow.getChildren().add(button);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            queryState.searchTerm = newVal == null ? "" : newVal;
            queryState.page = 1;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Reactions", "Reaction", new String[]{"User", "Target", "Type", "Count", "Updated"}, "No reactions found");
        });

        sortPill.setOnAction(e -> {
            queryState.ascending = !queryState.ascending;
            renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Reactions", "Reaction", new String[]{"User", "Target", "Type", "Count", "Updated"}, "No reactions found");
        });

        filterRow.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                styleQueryPill(view, button, false);
            }
        });

        renderForumTable(view, baseRows, queryState, tableHost, sortPill, headerControls, filterRow, "Forum Reactions", "Reaction", new String[]{"User", "Target", "Type", "Count", "Updated"}, "No reactions found");

        wrap.getChildren().addAll(stats, tableHost);
        return wrap;
    }

    private static HBox dashboardHeaderControls(TextField searchField, Button sortPill, HBox filterRow) {
        HBox headerControls = new HBox(8, searchField, sortPill, filterRow);
        headerControls.setAlignment(Pos.CENTER_LEFT);
        return headerControls;
    }

    private static void renderForumTable(
        DashboardView view,
        List<String[]> baseRows,
        DashboardTableQueryEngine.QueryState queryState,
        VBox tableHost,
        Button sortPill,
        HBox headerControls,
        HBox filterRow,
        String tableTitle,
        String entityLabel,
        String[] columns,
        String emptyMessage
    ) {
        String scope = queryState.filterKey;
        List<String[]> filteredRows = new ArrayList<>();
        for (String[] row : baseRows) {
            if (queryState.searchTerm == null || queryState.searchTerm.trim().isEmpty()) {
                filteredRows.add(row);
                continue;
            }

            String term = queryState.searchTerm.trim().toLowerCase();
            boolean matches = switch (scope) {
                case "title" -> row.length > 0 && row[0] != null && row[0].toLowerCase().contains(term);
                case "category" -> row.length > 1 && row[1] != null && row[1].toLowerCase().contains(term);
                case "publication" -> row.length > 0 && row[0] != null && row[0].toLowerCase().contains(term);
                case "author" -> row.length > 1 && row[1] != null && row[1].toLowerCase().contains(term);
                case "user" -> row.length > 0 && row[0] != null && row[0].toLowerCase().contains(term);
                case "target" -> row.length > 1 && row[1] != null && row[1].toLowerCase().contains(term);
                case "type" -> row.length > 2 && row[2] != null && row[2].toLowerCase().contains(term);
                case "date" -> row.length > 4 && row[4] != null && row[4].toLowerCase().contains(term);
                default -> {
                    boolean found = false;
                    for (String cell : row) {
                        if (cell != null && cell.toLowerCase().contains(term)) {
                            found = true;
                            break;
                        }
                    }
                    yield found;
                }
            };

            if (matches) {
                filteredRows.add(row);
            }
        }

        DashboardTableQueryEngine.QueryResult result = DashboardTableQueryEngine.apply(
            filteredRows,
            queryState,
            (row, key) -> true,
            0
        );

        String[][] visibleRows = result.pageRows.isEmpty()
            ? new String[][]{{emptyMessage, "-", "-", "-", "-"}}
            : result.pageRows.toArray(new String[0][]);

        sortPill.setText(queryState.ascending ? "Order: A-Z" : "Order: Z-A");
        tableHost.getChildren().clear();
        tableHost.getChildren().add(view.dataTableWithCrud(tableTitle, entityLabel, columns, visibleRows, true, headerControls));
    }

    private static TextField dashboardSearchField(DashboardView view, String promptText) {
        TextField searchField = new TextField();
        searchField.setPromptText(promptText);
        searchField.setPrefWidth(280);
        searchField.setFont(Font.font(view.lightFont(), FontWeight.NORMAL, 12));
        searchField.setStyle(
            "-fx-background-color:" + (view.isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
            "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (view.isDark() ? "white" : "#111827") + ";" +
            "-fx-prompt-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
        );
        return searchField;
    }

    private static void styleQueryPill(DashboardView view, Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                "-fx-background-color:" + view.accentRgba(1.0) + ";" +
                "-fx-text-fill:white;" +
                "-fx-border-width:0;" +
                "-fx-font-weight:bold;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:" + (view.isDark() ? "rgba(255,255,255,0.6)" : "rgba(15,23,42,0.6)") + ";" +
                "-fx-border-color:" + (view.isDark() ? "rgba(255,255,255,0.2)" : "rgba(15,23,42,0.2)") + ";" +
                "-fx-border-width:1;"
            );
        }
    }
}
