package com.syndicati.views.backend.dashboard;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Forum Publications", "Publication",
            new String[]{"Title", "Author", "Category", "Comments", "Date"},
            new String[][]{
                {"Reunion copropriete - Batiment A", "Ahmed B.", "Announcements", "12", "Mar 11"},
                {"Question sur les charges communes", "Leila M.", "General", "7", "Mar 10"},
                {"Electricite cage d'escalier", "Karim S.", "Issues", "4", "Mar 9"},
                {"Nouveau reglement interieur", "Sara A.", "Announcements", "18", "Mar 8"}
            }, true
        ));
        return wrap;
    }

    private static VBox commentsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Forum Comments", "Comment",
            new String[]{"Publication", "Author", "Snippet", "Likes", "Date"},
            new String[][]{
                {"Reunion copropriete", "Leila M.", "Merci pour le compte rendu.", "6", "Mar 11"},
                {"Charges communes", "Omar Z.", "Peut-on avoir le detail ?", "2", "Mar 10"},
                {"Reglement", "Karim S.", "C'est valide pour mon bloc.", "3", "Mar 9"}
            }, false
        ));
        return wrap;
    }

    private static VBox reactionsPane(DashboardView view) {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(view.dataTableWithCrud("Forum Reactions", "Reaction",
            new String[]{"User", "Target", "Type", "Count", "Updated"},
            new String[][]{
                {"Ahmed B.", "Publication #88", "Like", "24", "Mar 11"},
                {"Sara A.", "Comment #143", "Love", "9", "Mar 10"},
                {"Leila M.", "Publication #86", "Like", "17", "Mar 9"}
            }, false
        ));
        return wrap;
    }
}
