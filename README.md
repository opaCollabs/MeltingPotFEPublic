//FRONT-END

//SOS FOR UNDERSTANDING
.*<substring>$ (Ends with substring)

Τα παρακάτω αρχεία βρίσκονται στο εξής directory "app\src\main\java\com\example\meltingpot"

.*Activity$
Είναι αρχεία που περιέχουν τον κώδικα σε
Java που αφορά στα Activities της εφαρμογής στο Android
(σε σύνδεση με τα αντίστοιχα xml αρχεία στο res/layout)


.*Fragment$
Είναι αρχεία που περιέχουν τον κώδικα σε
Java που αφορά στα Fragments της εφαρμογής στο Android
(σε σύνδεση με τα αντίστοιχα xml αρχεία στο res/layout)


.*Adapter$
Είναι αρχεία που τροφοδοτούνε με περιεχόμενο τα Fragments,
χρησιμοποιούνται στα GridViews (RecyclerView)
όπου παιρνουνε τις οντότητες
και είναι υπεύθυνα για το τρόπο που παρουσιάζονται

.*$
Αρχεία κλάσεων για τη κατασκευή αντικειμένων χρησιμοποιούνται
και βοηθητικές κλάσεις για μικρές βελτιώσεις στο ui (CircularImageView)



Γενικότερος σχεδιασμός:
Πέρα από τη διαδικασία του login και του register που έχουν δικά
τους Activities,
η εφαρμογή λειτουργεί με ένα MainActivity 
το οποίο ορίζει ένα μενού στο κάτω μέρος της οθόνης (bottomNav)
μέσω του οποίου ο χρήστης πλογείται στις κύριες οθόνες της εφαρμγογής
που βλέπει οι οποίες είναι σε μορφή fragments και είναι τα:
 HomeFragment, 
 SearchFragment, 
 HealthFragment, 
 RecipesFragment και 
 ProfileFragment αντίστοιχα.
Στο σύνολο της, η λειτουργία της εφαρμογής πραγματοποιείται μέσω fragments, 
ενώ τα activities είναι πολύ συγκεκριμένα.
 
οι κλήσεις στο API γίνονται στα .*Activity$ και .*Fragment$ αρχεία