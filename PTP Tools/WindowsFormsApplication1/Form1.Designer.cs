namespace WindowsFormsApplication1
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.model_lb = new System.Windows.Forms.Label();
            this.firmware_lb = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.model_tb = new System.Windows.Forms.TextBox();
            this.firmware_tb = new System.Windows.Forms.TextBox();
            this.button2 = new System.Windows.Forms.Button();
            this.lang_lb = new System.Windows.Forms.Label();
            this.langage_tb = new System.Windows.Forms.TextBox();
            this.button3 = new System.Windows.Forms.Button();
            this.emaildev = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // model_lb
            // 
            this.model_lb.AutoSize = true;
            this.model_lb.Location = new System.Drawing.Point(12, 15);
            this.model_lb.Name = "model_lb";
            this.model_lb.Size = new System.Drawing.Size(46, 13);
            this.model_lb.TabIndex = 0;
            this.model_lb.Text = "Camera:";
            // 
            // firmware_lb
            // 
            this.firmware_lb.AutoSize = true;
            this.firmware_lb.Location = new System.Drawing.Point(12, 41);
            this.firmware_lb.Name = "firmware_lb";
            this.firmware_lb.Size = new System.Drawing.Size(52, 13);
            this.firmware_lb.TabIndex = 2;
            this.firmware_lb.Text = "Firmware:";
            // 
            // button1
            // 
            this.button1.Enabled = false;
            this.button1.Location = new System.Drawing.Point(12, 64);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(129, 24);
            this.button1.TabIndex = 4;
            this.button1.Text = "Backup Eeprom";
            this.button1.UseVisualStyleBackColor = true;
            // 
            // model_tb
            // 
            this.model_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.model_tb.Location = new System.Drawing.Point(73, 12);
            this.model_tb.Name = "model_tb";
            this.model_tb.ReadOnly = true;
            this.model_tb.Size = new System.Drawing.Size(204, 20);
            this.model_tb.TabIndex = 5;
            this.model_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // firmware_tb
            // 
            this.firmware_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.firmware_tb.Location = new System.Drawing.Point(73, 38);
            this.firmware_tb.Name = "firmware_tb";
            this.firmware_tb.ReadOnly = true;
            this.firmware_tb.Size = new System.Drawing.Size(204, 20);
            this.firmware_tb.TabIndex = 6;
            this.firmware_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // button2
            // 
            this.button2.Enabled = false;
            this.button2.Location = new System.Drawing.Point(147, 64);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(129, 24);
            this.button2.TabIndex = 7;
            this.button2.Text = "Restore Eeprom";
            this.button2.UseVisualStyleBackColor = true;
            // 
            // lang_lb
            // 
            this.lang_lb.AutoSize = true;
            this.lang_lb.Location = new System.Drawing.Point(12, 114);
            this.lang_lb.Name = "lang_lb";
            this.lang_lb.Size = new System.Drawing.Size(55, 13);
            this.lang_lb.TabIndex = 8;
            this.lang_lb.Text = "Language";
            // 
            // langage_tb
            // 
            this.langage_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.langage_tb.Location = new System.Drawing.Point(73, 111);
            this.langage_tb.Name = "langage_tb";
            this.langage_tb.ReadOnly = true;
            this.langage_tb.Size = new System.Drawing.Size(204, 20);
            this.langage_tb.TabIndex = 9;
            this.langage_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // button3
            // 
            this.button3.Enabled = false;
            this.button3.Location = new System.Drawing.Point(12, 137);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(129, 24);
            this.button3.TabIndex = 10;
            this.button3.Text = "Unlock Language";
            this.button3.UseVisualStyleBackColor = true;
            // 
            // emaildev
            // 
            this.emaildev.Location = new System.Drawing.Point(12, 167);
            this.emaildev.Name = "emaildev";
            this.emaildev.Size = new System.Drawing.Size(129, 23);
            this.emaildev.TabIndex = 11;
            this.emaildev.Text = "Email Developer";
            this.emaildev.UseVisualStyleBackColor = true;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(289, 202);
            this.Controls.Add(this.emaildev);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.langage_tb);
            this.Controls.Add(this.lang_lb);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.firmware_tb);
            this.Controls.Add(this.model_tb);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.firmware_lb);
            this.Controls.Add(this.model_lb);
            this.MaximumSize = new System.Drawing.Size(1024, 240);
            this.MinimumSize = new System.Drawing.Size(305, 240);
            this.Name = "Form1";
            this.Text = "LanguageFix";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label model_lb;
        private System.Windows.Forms.Label firmware_lb;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.TextBox model_tb;
        private System.Windows.Forms.TextBox firmware_tb;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label lang_lb;
        private System.Windows.Forms.TextBox langage_tb;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Button emaildev;
    }
}

